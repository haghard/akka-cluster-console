package console.components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.RouterCtl
import console.JsApplication.Route
import japgolly.scalajs.react.vdom.prefix_<^._

object MetricsModule {

  //val proxy: Proxy = AjaxClient[shared.ClusterApi]
  private val component = ReactComponentB[RouterCtl[Route]]("Metrics").stateless.render_P { props ⇒
    //println(props.byPath.baseUrl.value)
    <.div(^.cls := "container", ^.paddingTop := "6px")("Hello Metrics Module.")
  }.build

  def apply(r: RouterCtl[Route]) = component(r)
}
