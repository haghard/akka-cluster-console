package console.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ReactComponentB, ReactElement, ReactEventI}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.Document

import scala.util.{Failure, Success}

//https://japgolly.github.io/scalajs-react/#examples/websockets
object WebSocketsModule {

  case class WsState(ws: Option[WebSocket], logLines: Vector[String], message: String) {
    def log(line: String): WsState =
      copy(logLines = logLines :+ line)
  }

  class WsBackend(scope: BackendScope[_, WsState]) {

    def render(s: WsState): ReactElement = {
      // Can only send if WebSocket is connected and user has entered text
      val send: Option[Callback] = {
        for (ws ← s.ws if s.message.nonEmpty)
          yield sendMessage(ws, s.message)
      }

      <.div(
        <.h3("Type a message and get an echo:"),
        <.div(
          <.input(^.onChange ==> onChange, ^.value := s.message),
          <.button(
            ^.disabled := send.isEmpty, // Disable button if unable to send
            ^.onClick -->? send,        // --> suffixed by ? because it's for Option[Callback]
            "Send"
          )
        ),
        <.h4("Connection log"),
        <.pre(^.width := 360.px, ^.height := 200.px, ^.border := "1px solid", s.logLines.map(<.p(_)))
      )
    }

    def onChange(e: ReactEventI): Callback = {
      val newMessage = e.target.value
      scope.modState(_.copy(message = newMessage))
    }

    def sendMessage(ws: WebSocket, msg: String): Callback = {
      def send = Callback {
        ws.send(msg)
      }

      def updateState = scope.modState(s ⇒ s.log(s"Sent: ${s.message}").copy(message = ""))

      send >> updateState
    }

    def start(url: String): Callback = {
      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {
        // Get direct access so WebSockets API can modify state directly
        // (for access outside of a normal DOM/React callback).
        val direct = scope.accessDirect

        // These are message-receiving events from the WebSocket "thread".
        def onOpen(e: Event): Unit =
          direct.modState(_.log("Connected."))

        def onMessage(e: MessageEvent): Unit =
          direct.modState(_.log(s"Echo: ${e.data.toString}"))

        def onError(e: org.scalajs.dom.raw.Event): Unit =
          direct.modState(_.log(s"Error: ${e}"))

        def onClose(e: CloseEvent): Unit =
          direct.modState(_.copy(ws = None).log(s"Closed: ${e.reason}"))

        // Create WebSocket and setup listeners
        val ws = new WebSocket(url)
        ws.onopen = onOpen _
        ws.onclose = onClose _
        ws.onmessage = onMessage _
        ws.onerror = onError _
        ws
      }

      // Here use attemptTry to catch any exceptions in connect.
      connect.attemptTry.flatMap {
        case Success(ws) ⇒
          scope.modState {
            _.log("Connecting...").copy(ws = Option(ws))
          }
        case Failure(error) ⇒
          scope.modState {
            _.log(error.toString)
          }
      }
    }

    def onComplete: Callback = {
      def closeWebSocket = scope.state.map(_.ws.foreach(_.close()))

      def clearWebSocket = scope.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
    }
  }

  def apply(clusterName: String) =
    ReactComponentB[Unit]("WebSockets")
      .initialState(WsState(None, Vector.empty, ""))
      .renderBackend[WsBackend]
      .componentDidMount(_.backend.start(wsUri(dom.document)))
      .componentWillUnmount(_.backend.onComplete)
      .build

  private def wsUri(document: Document): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    val wsUrl      = s"$wsProtocol://${dom.document.location.host}/events"
    println(wsUrl)
    wsUrl
  }
}
