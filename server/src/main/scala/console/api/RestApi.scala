package console.api

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.headers.{Host, `X-Forwarded-For`, `X-Real-Ip`}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.ByteString
import console.scripts.AppScript

object RestApi extends Directives {

  private val folderName = "public"

  /*pathSingleSlash {
    //get(encodeResponse(getFromDirectory(folderName + "/" + chartHtml)))
    //encodeResponse(getFromResource(folderName + "/monitor3.html"))
  } ~*/

  def route(url: String): Route =
    //route ~
    extractLog { log ⇒
      path("console") {
        (optionalHeaderValueByType[Host]() & optionalHeaderValueByType[`X-Real-Ip`]() & optionalHeaderValueByType[
          `X-Forwarded-For`
        ]()) { (host, ip, ipFrw) ⇒
          complete {
            log.info("GET from: {}/{}/{}", host, ip, ipFrw)
            HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(AppScript(url).render)))
          }
        }
      } ~ pathPrefix("console-assets" / Remaining) { file ⇒
        encodeResponse(getFromResource(folderName + "/" + file))
      }
    }
}
