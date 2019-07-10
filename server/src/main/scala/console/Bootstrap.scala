package console

import Bootstrap._
import akka.http.scaladsl.server.RouteResult._
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.stream.Materializer
import akka.Done
import akka.actor.CoordinatedShutdown.{PhaseServiceRequestsDone, PhaseServiceUnbind, Reason}
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Bootstrap {
  val HttpDispatcher = "akka.http.dispatcher"
  final private case object BindFailure extends Reason
}

case class Bootstrap(interface: String, port: Int)(implicit system: ActorSystem, m: Materializer) {
  val termDeadline = 2.seconds
  implicit val ex = system.dispatchers.lookup(HttpDispatcher)
  val shutdown     = CoordinatedShutdown(system)

  Http()
    .bindAndHandle(api.RestApi.route(system, m), interface, port)
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
    }(ExecutionContext.global)
}