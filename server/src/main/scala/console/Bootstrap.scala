package console

import Bootstrap._
import akka.http.scaladsl.server.RouteResult._
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.stream.Materializer
import akka.Done
import akka.actor.CoordinatedShutdown.{PhaseServiceRequestsDone, PhaseServiceUnbind, Reason}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT}
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.ExecutionDirectives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Bootstrap {
  final private case object BindFailure extends Reason
}

case class Bootstrap(interface: String, port: Int, url: String)(implicit system: ActorSystem, m: Materializer) {
  val termDeadline = 2.seconds
  val shutdown     = CoordinatedShutdown(system)
  implicit val ex  = system.dispatchers.lookup(Application.HttpDispatcher)

  //This will ensure that only Javascript running on the {interface} domain can talk to the webserver
  val corsAllowedMethods = immutable.Seq(GET, POST, HEAD, OPTIONS, PATCH, PUT, DELETE)
  val corsSettings = CorsSettings(system)
    .withAllowedMethods(corsAllowedMethods)
    .withAllowedOrigins(HttpOriginMatcher(HttpOrigin(s"http://${interface}:${port}")))

  val corsRoute: Route =
    handleRejections(corsRejectionHandler)(cors(corsSettings)(api.RestApi.route(url)))

  Http()
    .bindAndHandle(corsRoute, interface, port)
    .onComplete {
      case Failure(ex) ⇒
        system.log.error(ex, "Critical error during bootstrap")
        shutdown.run(BindFailure)
      case Success(binding) ⇒
        system.log.info(s"Listening for HTTP connections on ${binding.localAddress}")
        shutdown.addTask(PhaseServiceUnbind, "api.unbind") { () ⇒
          system.log.info("api.unbind")
          // No new connections are accepted
          // Existing connections are still allowed to perform request/response cycles
          binding.unbind()
        }

        shutdown.addTask(PhaseServiceRequestsDone, "api.terminate") { () ⇒
          system.log.info("api.terminate")
          //graceful termination request being handled on this connection
          binding.terminate(termDeadline).map(_ ⇒ Done)(ExecutionContext.global)
        }
    }
}
