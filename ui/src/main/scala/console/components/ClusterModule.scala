package console.components

import console.JsApplication.Route
import japgolly.scalajs.react
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}

import scala.concurrent.Future

object ClusterModule {

  case class Props(psw: String, r: RouterCtl[Route])
  case class Cluster(name: String = "", leader: String)
  case class State(cluster: Option[Cluster] = None)

  class ClusterBackend(scope: BackendScope[Props, State]) {
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

    def fetchClusters(p: Props): japgolly.scalajs.react.CallbackTo[Unit] =
      react.Callback.future {
        Future.successful(scope.modState(_.copy(cluster = Some(Cluster("demo", "leader")))))
      }

    /*
      import autowire._
      react.Callback.future {
        p.proxy.clusterInfo().call().map { info ⇒
          dom.console.log(s"fetch-cluster: ${info.toString}")
          scope.modState(_.copy(cluster = Some(Cluster(info.name, info.seedNodes))))
        }
      }
     */

    def render(state: State, p: Props): ReactElement =
      state.cluster.fold(<.div())(c ⇒ <.div(GraphModule(c.name, p.psw)))
    //state.cluster.fold(<.div())(c ⇒ <.div(WebSocketsModule(c.name)()))
  }

  private val component = ReactComponentB[Props]("Cluster-Module")
    .initialState(State())
    .backend(new ClusterBackend(_))
    .renderPS((scope, props, state) ⇒ scope.backend.render(state, props))
    .componentDidMount(scope ⇒ scope.backend.fetchClusters(scope.props))
    .build

  def apply(psw: String, r: RouterCtl[Route]) =
    component(Props(psw, r))
}
