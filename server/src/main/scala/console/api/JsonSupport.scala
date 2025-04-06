package console.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._
  implicit val fmt: RootJsonFormat[ChatMessage] = jsonFormat1(ChatMessage.apply)
}
