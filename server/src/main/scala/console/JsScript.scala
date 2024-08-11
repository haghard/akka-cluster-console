package console

import scalatags.Text.all._

object JsScript {

  val targetDiv = "placeHolder"

  def apply(system: String, url: String) =
    html(
      head(),
      body(
        link(rel := "stylesheet", href := "/console-assets/lib/bootstrap/css/bootstrap.css"),
        // link(rel      := "stylesheet", href     := "/console-assets/lib/bootstrap/css/main.css"),
        script(`type` := "text/javascript", src := "/console-assets/lib/jquery/jquery.js"),
        script(`type` := "text/javascript", src := "/console-assets/lib/bootstrap/js/bootstrap.js"),
        script(`type` := "text/javascript", src := "/console-assets/ui-jsdeps.js"),
        // script(`type` := "text/javascript", src := "/console-assets/ui-fastopt.js"),
        script(`type` := "text/javascript", src := "/console-assets/ui-opt.js"),
        div(id        := targetDiv, style       := "position:relative"),
        script(s"JsApp(`$system`,`$url`,'$targetDiv')")
      )
    )
}
