package console

import console.style.{GlobalStyles, GraphStyles}
import japgolly.scalajs.react.ReactDOM
import console.components.{ClusterModule, MainMenu, MetricsModule}
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Resolution, Router, RouterConfigDsl, RouterCtl}
import org.scalajs.dom
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js.annotation.JSExport

//https://github.com/japgolly/scalajs-react/blob/0e984d1fd57fb35106bc8c3ec5d2566800a7e9a8/gh-pages/src/main/scala/ghpages/ExtrasExamples.scala

@JSExport
object JsApplication {

  sealed trait Route
  case object DashboardRoute  extends Route
  case object ClusterMapRoute extends Route

  def routerConfig(url: String) =
    RouterConfigDsl[Route]
      .buildConfig { dsl ⇒
        import dsl._

        (staticRoute(root, DashboardRoute) ~> renderR(ctl ⇒ MetricsModule(ctl))
        | staticRoute("#cluster", ClusterMapRoute) ~> renderR(ctl ⇒ ClusterModule(url, ctl)))
          .notFound(redirectToPage(DashboardRoute)(Redirect.Replace))
      }
      .renderWith(layout)

  def layout(c: RouterCtl[Route], r: Resolution[Route]) =
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container-fluid")(
          <.div(^.className := "navbar-header")(<.span(^.className := "navbar-brand")("Console")),
          <.div(^.className := "collapse navbar-collapse")(MainMenu(MainMenu.Props(c, r.page)))
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container-fluid")(r.render())
    )

  @JSExport
  def main(url: String): Unit = {
    import scalacss.Defaults._
    scalacss.internal.mutable.GlobalRegistry.onRegistration { s ⇒
      val style: StyleA = s.styles.head
    //println(style.render[String])
    //println(new GraphStyles().render[String])
    //println(style.className.value)
    }

    GlobalStyles.addToDocument()
    scalacss.internal.mutable.GlobalRegistry.register(new GraphStyles)

    val router = Router(BaseUrl.until_#, routerConfig(url))
    ReactDOM.render(router(), dom.document.getElementById("scene"))
  }
}
