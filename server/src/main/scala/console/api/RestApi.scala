package console.api

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.Directives.getFromResource
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.ByteString
import console.scripts.AppScript

object RestApi extends Directives {

  private val folderName = "public"

  /*pathSingleSlash {
    //get(encodeResponse(getFromDirectory(folderName + "/" + chartHtml)))
    //encodeResponse(getFromResource(folderName + "/monitor3.html"))
  } ~*/

  def routes(url: String): Route =
    extractActorSystem { system =>
      extractLog { log =>
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
            log.info("GET {}", file)
            // akka.jpg
            encodeResponse(getFromResource(folderName + "/" + file))
          }
      } ~ akka.management.cluster.scaladsl.ClusterHttpManagementRoutes(akka.cluster.Cluster(system))
    }

}
