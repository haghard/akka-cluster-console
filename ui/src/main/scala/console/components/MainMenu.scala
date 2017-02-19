package console.components

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all.{className, li, ul}
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactNode}
import console.JsApplication.{ClusterMapRoute, DashboardRoute, Route}
import console.style.{GlobalStyles, Icon}
import console.style.Icon.Icon
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^.<

import scalacss.ScalaCssReact._

object MainMenu {

  @inline private def style = GlobalStyles.bootstrapStyles

  case class Props(ctl: RouterCtl[Route], currentRoute: Route)

  case class MenuItem(label: (Props) => ReactNode, icon: Icon, location: Route)

  class MainMenuBackend(t: BackendScope[Props, Unit]) extends OnUnmount {
    val menuItems = Seq(
      MenuItem(_ => "Metrics", Icon.dashboard, DashboardRoute),
      MenuItem(_ => "Clusters", Icon.circle, ClusterMapRoute))

    def render(props: Props) = {
      <.ul(style.navbar)(
        for (item <- menuItems) yield {
          <.li(
            (props.currentRoute == item.location) ?= (className := "active"),
            props.ctl.link(item.location)(item.icon, " ", item.label(props))
          )
        }
      )
    }
  }

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .renderBackend[MainMenuBackend]
    .build

  def apply(props: Props) = MainMenu(props)
}