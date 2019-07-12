package console.api

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.headers.{Host, `X-Forwarded-For`, `X-Real-Ip`}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.ByteString
import console.scripts.AppScript

object RestApi extends Directives {

  //pathSingleSlash {
  def route(pws: String): Route =
    extractLog { log ⇒
      path("console") {
        (optionalHeaderValueByType[`X-Real-Ip`]() & optionalHeaderValueByType[Host]() & optionalHeaderValueByType[
          `X-Forwarded-For`
        ]()) { (ip, host, ipFor) ⇒
          complete {
            log.info("GET from: {}/{}/{}", ip, host, ipFor)
            HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(AppScript(pws).render)))
          }
        }
      } ~ pathPrefix("console-assets" / Remaining) { file ⇒
        encodeResponse(getFromResource("public/" + file))
      }
    }
}