package console.components

import console.JsApplication.Route
import japgolly.scalajs.react
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}

object ClusterModule {

  case class Props(proxy: Proxy, r: RouterCtl[Route])
  case class Cluster(name: String = "", seedNodes: Seq[String] = Seq.empty[String])
  case class State(cluster: Option[Cluster] = None)

  class ClusterBackend(scope: BackendScope[Props, State]) {
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

    def fetchClusters(p: Props): japgolly.scalajs.react.CallbackTo[Unit] = {
      import autowire._
      react.Callback.future {
        p.proxy.clusterInfo().call().map { c =>
          scope.modState { _.copy(cluster = Some(Cluster(c.name, c.seedNodes))) }
        }
          /*.recover {
          case e: org.scalajs.dom.ext.AjaxException =>
            //scope.modState(s => s.copy(name = "Cluster-list error")).runNow()
          case NonFatal(e) =>
            //scope.modState(s => s.copy(name = "Unexpected cluster-list error")).runNow()
        }*/
      }
    }


    def render(state: State, p: Props): ReactElement = {
      state.cluster.fold(<.div()) { c => <.div(GraphModule(c.name, p.proxy)) }
    }
  }

  val component = ReactComponentB[Props]("ClusterModule")
    .initialState(State())
    .backend(new ClusterBackend(_))
    .renderPS((scope, props, state) => scope.backend.render(state, props))
    .componentDidMount(scope => scope.backend.fetchClusters(scope.props))
    .build

  def apply(r: RouterCtl[Route]) = {
    val proxy: Proxy = AjaxClient[shared.ClusterApi]
    component(Props(proxy, r))
  }
}
