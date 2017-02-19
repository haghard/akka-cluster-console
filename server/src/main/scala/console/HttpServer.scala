package console

import akka.stream.ActorMaterializerSettings
import akka.actor.{Actor, ActorLogging, Status}

object HttpServer {
  val HttpDispatcher = "akka.http.dispatcher"
  object Stop
}

class HttpServer(port: Int, address: String, keypass: String, storepass: String) extends Actor with ActorLogging {
  import HttpServer._
  import akka.http.scaladsl.Http
  import akka.pattern.pipe
  import akka.http.scaladsl.server.RouteResult._
  import akka.http.scaladsl.server.RouteConcatenation._

  implicit val system = context.system
  implicit val ex = system.dispatchers.lookup(HttpDispatcher)
  implicit val mat = akka.stream.ActorMaterializer(
    ActorMaterializerSettings.create(system)
      .withDispatcher(HttpDispatcher))(system)

  val routes = api.RestApi.route(system, mat)

  //connectionContext = https(keypass, storepass))
  Http().bindAndHandle(routes, address, port).pipeTo(self)

  override def receive = {
    case b: akka.http.scaladsl.Http.ServerBinding => serverBinding(b)
    case Status.Failure(c) => handleBindFailure(c)
  }

  def serverBinding(b: akka.http.scaladsl.Http.ServerBinding) = {
    log.info("Binding on {}",  b.localAddress)
    context become bound(b)
  }

  def handleBindFailure(cause: Throwable) = {
    log.error(cause, s"Can't bind to $address:$port!")
    (context stop self)
  }

  def bound(b: akka.http.scaladsl.Http.ServerBinding): Receive = {
    case HttpServer.Stop =>
      log.info("Unbound {}:{}", address, port)
      b.unbind().onComplete { _ =>  mat.shutdown }
  }
}