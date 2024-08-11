package console.api

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.Directives.getFromResource
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.ByteString
import console.JsScript
import console.scripts.AppScript

object RestApi extends Directives {

  private val folderName = "public"

  def clusterConsolePage(system: String, url: String) =
    pathSingleSlash {
      get {
        complete {
          HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(JsScript(system, url).render)))
        }
      }
    }

  def routes(
    systemName: String,
    url: String
  ): Route =
    extractActorSystem { system =>
      extractLog { log =>
        clusterConsolePage(systemName, url) ~
          path("pics" / Remaining) { _ =>
            getFromResource("akka-small.jpg")
          } ~
          path("console") {
            (optionalHeaderValueByType(Host) & optionalHeaderValueByType(`X-Real-Ip`) & optionalHeaderValueByType(
              `X-Forwarded-For`
            )) { (host, ip, ipF) =>
              complete {
                log.info("GET console {}/{}/{}", host, ip, ipF)
                HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(AppScript(url).render)))
              }
            }
          } ~ pathPrefix("console-assets" / Remaining) { file =>
            // log.info("GET {}", file)
            encodeResponse(getFromResource(folderName + "/" + file))
          }
      } ~ akka.management.cluster.scaladsl.ClusterHttpManagementRoutes(akka.cluster.Cluster(system))
    }

}
