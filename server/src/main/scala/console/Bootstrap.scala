package console

import akka.Done
import akka.actor.ActorSystem
import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.PhaseServiceRequestsDone
import akka.actor.CoordinatedShutdown.PhaseServiceUnbind
import akka.actor.CoordinatedShutdown.Reason
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.DELETE
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpMethods.HEAD
import akka.http.scaladsl.model.HttpMethods.OPTIONS
import akka.http.scaladsl.model.HttpMethods.PATCH
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpMethods.PUT
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult._
import akka.http.scaladsl.server.directives.ExecutionDirectives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.corsRejectionHandler
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

import Bootstrap._

object Bootstrap {
  final private case object BindFailure extends Reason
}

case class Bootstrap(host: String, port: Int, clusterUrl: String)(implicit
  system: ActorSystem
) {
  import system.dispatcher

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
