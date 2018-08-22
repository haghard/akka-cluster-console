package console

import akka.stream.ActorMaterializerSettings
import akka.http.scaladsl.Http.ServerBinding
import akka.actor.{Actor, ActorLogging, Props, Status}

object Bootstrap {
  val HttpDispatcher = "akka.http.dispatcher"

  def prop(port: Int, address: String, keypass: String, storepass: String, sslFile: String) =
    Props(new Bootstrap(port, address, keypass, storepass, sslFile))
      .withDispatcher(HttpDispatcher)
}

class Bootstrap(port: Int, address: String, keypass: String, storepass: String,
  override val sslFile: String) extends Actor with ActorLogging
  with SslSupport {

  import Bootstrap._
  import akka.http.scaladsl.Http
  import akka.pattern.pipe
  import akka.http.scaladsl.server.RouteResult._

  implicit val system = context.system
  implicit val ex = system.dispatchers.lookup(HttpDispatcher)
  implicit val mat = akka.stream.ActorMaterializer(
    ActorMaterializerSettings.create(system)
      .withDispatcher(HttpDispatcher))(system)

  override def preStart() =
    Http()
      .bindAndHandle(api.RestApi.route(system, mat), address, port)
      .pipeTo(self)

  def awaitHttpBinding(): Receive = {
    case b: ServerBinding =>
      log.info("Bind on {}", b.localAddress)

    case Status.Failure(ex) =>
      log.error(ex, s"Can't bind to $address:$port")
      context stop self
  }

  override def receive = awaitHttpBinding
}