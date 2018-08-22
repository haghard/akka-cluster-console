package console.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, GraphDSL, Keep, MergeHub, MergePreferred, Source}
import akka.util.ByteString
import console.scripts.AppScript
import upickle.Js

import scala.concurrent.duration.FiniteDuration

object RestApi extends shared.ClusterApi with Directives {

  val bufferSize = 1 << 8

  private def heartbeats[T](interval: FiniteDuration, zero: T): Flow[T, T, akka.NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
      import GraphDSL.Implicits._
      val heartbeats = builder.add(Source.repeat(zero).delay(interval, OverflowStrategy.dropBuffer))
      //0 - preferred port
      //1 - secondary port
      val merge = builder.add(MergePreferred[T](1))
      heartbeats ~> merge.in(0)//heartbeats is not preferred port
      FlowShape(merge.preferred, merge.out)
    })
  }

  //http://doc.akka.io/docs/akka/current/scala/stream/stream-dynamic.html#Dynamic_fan-in_and_fan-out_with_MergeHub_and_BroadcastHub
  /**
    * A MergeHub allows to implement a dynamic fan-in junction point in a graph where elements coming from different producers are emitted
    * in a First-Comes-First-Served fashion. If the consumer cannot keep up then all of the producers are backpressured.
    * The hub itself comes as a Source to which the single consumer can be attached. It is not possible to attach any producers until this
    * Source has been materialized (started). This is ensured by the fact that we only get the corresponding Sink as a materialized value.
    * Usage might look like this:
    */
  private def sourceAndSink(system: ActorSystem, mat: akka.stream.ActorMaterializer) = {
    MergeHub.source[Message](perProducerBufferSize = bufferSize)
      .recoverWithRetries(-1, { case _: Exception => Source.empty })
      .toMat(BroadcastHub.sink[Message](bufferSize))(Keep.both).run()(mat)
  }

  val pingMsg = TextMessage.Strict("ping")

  def route(implicit system: ActorSystem, mat: akka.stream.ActorMaterializer): Route = {
    import scala.concurrent.duration._
    val (sink, source) = sourceAndSink(system, mat)
    val wsFlow = Flow[Message].via(Flow.fromSinkAndSource(sink, source via heartbeats(25.seconds, pingMsg)))
      .watchTermination() { (_, termination) =>
        termination.foreach { _ =>
          system.log.info("ws-flow events has been terminated")
        }(mat.executionContext)
        NotUsed
      }

    extractExecutionContext { implicit ec =>
      pathSingleSlash {
        complete(HttpResponse(entity = Strict(ContentTypes.`text/html(UTF-8)`, ByteString(AppScript().render))))
      } ~ pathPrefix("assets" / Remaining) { file =>
        encodeResponse(getFromResource("public/" + file))
      } ~ path("images" / Segment) { image =>
        encodeResponse(getFromResource("web/" + image))
      } ~ post {
        path(shared.Routes.pref / Segments) { s =>
          entity(as[String]) { e =>
            complete {
              AutowireServer.route[shared.ClusterApi](RestApi)(
                autowire.Core.Request(s, upickle.json.read(e).asInstanceOf[Js.Obj].value.toMap)
              ).map(upickle.json.write(_))
            }
          }
        }
      } ~ path("events") {
        handleWebSocketMessages(wsFlow)
      }
    }
  }

  override def clusterInfo(): shared.protocol.ClusterInfo = {
    shared.protocol.ClusterInfo("demo-cluster",
      scala.collection.immutable.Seq(
        "akka.tcp://scenter@192.168.0.62:2551",
        "akka.tcp://scenter@192.168.0.62:2552"))
  }

  override def clusterProfile(): shared.protocol.ClusterProfile = {
    shared.protocol.ClusterProfile("demo-cluster",
      Set(shared.protocol.HostPort("192.168.0.62", 2551), shared.protocol.HostPort("192.168.0.63", 2551)), "Up",
      Set(
        shared.protocol.ClusterMember(shared.protocol.HostPort("192.168.0.62", 2551), Set("gateway"), shared.protocol.Up),
        shared.protocol.ClusterMember(shared.protocol.HostPort("192.168.0.63", 2551), Set("gateway"), shared.protocol.Up),
        shared.protocol.ClusterMember(shared.protocol.HostPort("192.168.0.62", 2552), Set("ms-cmd"), shared.protocol.Up),
        shared.protocol.ClusterMember(shared.protocol.HostPort("192.168.0.63", 2552), Set("ms-view"), shared.protocol.Up),
        shared.protocol.ClusterMember(shared.protocol.HostPort("192.168.0.64", 2551), Set("ms-view"), shared.protocol.Up)
      )
    )
  }
}