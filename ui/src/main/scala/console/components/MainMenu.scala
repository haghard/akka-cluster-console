package console
package components

import console.JsAppModule.{ClusterMapRoute, DashboardRoute, Route}
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all.className
import japgolly.scalajs.react.BackendScope
import console.style.{GlobalStyles, Icon}
import console.style.Icon.Icon
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scalacss.ScalaCssReact._

object MainMenu {

  @inline private def style = GlobalStyles.bootstrapStyles

  final case class Props(ctl: RouterCtl[Route], currentRoute: Route)

  final case class MenuItem(label: (Props) ⇒ japgolly.scalajs.react.vdom.TagMod, icon: Icon, location: Route)

  class MainMenuBackend(t: BackendScope[Props, Unit]) extends OnUnmount {

    //"Metrics"
    //VdomNode("Clusters")
    private val menuItems = Seq(
      MenuItem(_ ⇒ japgolly.scalajs.react.vdom.VdomNode("Metrics"), Icon.dashboard, DashboardRoute),
      MenuItem(_ ⇒ japgolly.scalajs.react.vdom.VdomNode("Clusters"), Icon.circle, ClusterMapRoute)
    )

    def render(props: Props) = {
      val elements: Seq[japgolly.scalajs.react.vdom.all.TagMod] =
        for (item ← menuItems) yield {
          <.li(
            //(if (props.currentRoute == item.location) className := "active"  else
            props.ctl
              .link(item.location)(item.icon, japgolly.scalajs.react.vdom.TagMod.empty, item.label(props))
            //: japgolly.scalajs.react.vdom.all.TagMod
          )
        }
      <.ul(style.navbar)(elements: _*) //japgolly.scalajs.react.vdom.TagMod(elements))
    }

    /*
    def render(props: Props) =
          <.ul(style.navbar)(
            for (item ← menuItems) yield {
              <.li(
                (props.currentRoute == item.location) ?= (className := "active"),
                props.ctl.link(item.location)(item.icon, " ", item.label(props))
              )
            }
          )
     */
    /*
      <.ul(style.navbar)(
        for (item ← menuItems) yield {
          <.li(
            if (props.currentRoute == item.location) (className := "active")
            else props.ctl.link(item.location)(item.icon, " ", item.label(props))
          )
        }
      )*/
  }

  private val MainMenu = ScalaComponent
    .builder[Props]("MainMenu")
    .renderBackend[MainMenuBackend]
    .build

  def apply(props: Props) =
    MainMenu(props)
}
