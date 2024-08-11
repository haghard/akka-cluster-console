package console

import Bootstrap._
import akka.http.scaladsl.server.RouteResult._
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.Done
import akka.actor.CoordinatedShutdown.{PhaseServiceRequestsDone, PhaseServiceUnbind, Reason}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.ExecutionDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.corsRejectionHandler

object Bootstrap {
  final private case object BindFailure extends Reason
}

case class Bootstrap(host: String, port: Int, clusterUrl: String)(implicit system: ActorSystem) {
  implicit val ex = system.dispatchers.lookup(Application.HttpDispatcher)

  val terminationDeadline = FiniteDuration(
    system.settings.config.getDuration("akka.coordinated-shutdown.default-phase-timeout").toNanos,
    NANOSECONDS
  )

  val shutdown = CoordinatedShutdown(system)

  val corsAllowedMethods = immutable.Seq(GET, POST, HEAD, OPTIONS, PATCH, PUT, DELETE)
  val corsSettings       = CorsSettings.defaultSettings.withAllowedMethods(corsAllowedMethods)
  val routes: Route = handleRejections(corsRejectionHandler)(
    cors(corsSettings)(
      api.RestApi.routes(system.name, clusterUrl)
    )
  )

  Http()
    .newServerAt(host, port)
    .bindFlow(routes)
    // .bindFlow(cors()(api.RestApi.routes(system.name, clusterUrl)))
    .onComplete {
      case Failure(ex) =>
        system.log.error(ex, "Critical error during bootstrap")
        shutdown.run(BindFailure)
      case Success(binding) =>
        system.log.info(s"Listening for HTTP connections on ${binding.localAddress}")
        shutdown.addTask(PhaseServiceUnbind, "api.unbind") { () =>
          system.log.info("api.unbind")
          // No new connections are accepted
          // Existing connections are still allowed to perform request/response cycles
          binding.unbind()
        }

        shutdown.addTask(PhaseServiceRequestsDone, "api.terminate") { () =>
          system.log.info("api.terminate")
          // graceful termination request being handled on this connection
          binding.terminate(terminationDeadline).map(_ => Done)(ExecutionContext.global)
        }
    }
}
