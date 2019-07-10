package console
package components

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

object MetricsModule {

  //val proxy: Proxy = AjaxClient[shared.ClusterApi]
  private val component = ScalaComponent
    .builder[RouterCtl[JsAppModule.Route]]("Metrics")
    .stateless
    .render_P { props ⇒
      //println(props.byPath.baseUrl.value)
      <.div(^.cls := "container", ^.paddingTop := "6px")("Hello Metrics Module.")
    }
    .build

  def apply(r: RouterCtl[JsAppModule.Route]) =
    component(r)
}
