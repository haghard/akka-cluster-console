package console
package components

import console.JsAppModule.{ClusterMapRoute, DashboardRoute, Route}
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all.{className, li, ul}
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.vdom.html_<^.{VdomElement, VdomStyle, _}
import console.style.{GlobalStyles, Icon}
import console.style.Icon.Icon
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._

import scalacss.ScalaCssReact._

object MainMenu {

  @inline private def style = GlobalStyles.bootstrapStyles

  final case class Props(ctl: RouterCtl[Route], currentRoute: Route)

  final case class MenuItem(label: (Props) ⇒ String, icon: Icon, location: Route)

  class MainMenuBackend(t: BackendScope[Props, Unit]) extends OnUnmount {

    val menuItems = Seq(
      MenuItem(_ ⇒ "Metrics", Icon.dashboard, DashboardRoute),
      MenuItem(_ ⇒ "Clusters", Icon.circle, ClusterMapRoute)
    )

    def render(props: Props) =
      <.ul(style.navbar)(
        for (item ← menuItems) yield {
          <.li(
            //(props.currentRoute == item.location)(className := "active"),
            props.ctl.link(item.location)(item.icon, " ", item.label(props))
          )
        }
      )

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
