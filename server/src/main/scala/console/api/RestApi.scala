package console.api

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import console.scripts.JsScript

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object RestApi extends Directives with JsonSupport {

  private val folderName = "public"

  def clusterConsolePage(system: String, url: String) =
    pathSingleSlash {
      get {
        complete {
          HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(JsScript(system, url).render)))
        }
      }
    }

  // val replicas: SortedMultiDict[String, Replica] = SortedMultiDict.empty[String, Replica],

  def flow(implicit ec: ExecutionContext) = {
    val circle = Set("a", "b", "c").foldLeft(CropCircle("cluster")) { (circle, c) =>
      circle :+ (c, "aaaa") :+ (c, "bbbb") :+ (c, "cccc")
    }

    val f =
      Flow.fromSinkAndSourceCoupled[Message, Message](
        Sink.ignore,
        Source.tick(0.second, 5.second, TextMessage.Strict(circle.toString))
      )

    f.watchTermination()((_, d) => d.onComplete(_ => println("Done")))
    f
  }

  def cropCircleView1(ec: ExecutionContext) =
    get(path("crop-circle1")(getFromResource("view1.html"))) ~
      path("d3" / Remaining) { file =>
        encodeResponse(getFromResource("d3/" + file))
      }

  // Take a look at path("view")(get(encodeResponse(getFromFile(folderName + "/" + circlePage))))
  def cropCircleView(ec: ExecutionContext) =
    get(path("crop-circle")(getFromResource("view.html"))) ~
      path("d3.v5.js")(get(encodeResponse(getFromResource("d3.v5.js")))) ~
      path("events")(handleWebSocketMessages(flow(ec)))

  def monitorView =
    get(path("monitor")(getFromResource("monitor/monitor.html")))

  def monitorView2 =
    get(path("monitor2")(getFromResource("monitor/monitor2.html")))

  def monitorView3 =
    get(path("monitor3")(getFromResource("monitor/monitor3.html")))

  def chatView =
    get(path("mychat")(getFromResource("chat/index.html"))) ~
      path("chat" / Remaining) { file =>
        encodeResponse(getFromResource("chat/" + file))
      }

  //  /chat/completions
  def completions =
    path("chat" / "messages") {
      post {
        entity(as[ChatMessage]) { req =>
          complete {
            println("*****" + req)
            // """{ "data": "1.asasasfasfasfasfasfa", "choices": [{"finish_reason":"1"}], "context":"2222" }"""
            // parsedResponse.choices[0].delta.content
            s"""{ "choices":[{"finish_reason":null, "delta":{"content":"Echo: ${req.message} at ${System
                .currentTimeMillis()}"}}]}"""
          }
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
            encodeResponse(getFromResource("akka-small.jpg"))
          } ~ pathPrefix("assets" / Remaining) { file =>
            log.info("GET assets {}", file)
            encodeResponse(getFromResource(folderName + "/" + file))
          }
      } ~ akka.management.cluster.scaladsl.ClusterHttpManagementRoutes(akka.cluster.Cluster(system)) ~
        cropCircleView(system.dispatcher) ~
        cropCircleView1(system.dispatcher) ~
        monitorView ~ monitorView2 ~ monitorView3 ~ chatView ~ completions
    }

}
