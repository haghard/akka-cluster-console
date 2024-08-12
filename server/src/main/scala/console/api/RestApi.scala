package console.api

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import console.scripts.JsScript

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
          } ~ pathPrefix("assets" / Remaining) { file =>
            log.info("GET assets {}", file)
            encodeResponse(getFromResource(folderName + "/" + file))
          }
      } ~ akka.management.cluster.scaladsl.ClusterHttpManagementRoutes(akka.cluster.Cluster(system))
    }

}
