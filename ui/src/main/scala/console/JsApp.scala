package console

import console.components.ClusterViewModule
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExportTopLevel

object JsApp {

  @JSExportTopLevel("JsApp")
  def run(system: String, url: String, targetDiv: String): Unit =
    ClusterViewModule(system, url).renderIntoDOM(
      document.getElementById(targetDiv)
    )
}
