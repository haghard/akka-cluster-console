package console.scripts

import scalatags.Text.all._

object JsScript {

  val targetDiv = "placeHolder"

  def apply(system: String, url: String) =
    html(
      head(),
      body(
        link(rel      := "stylesheet", href     := "/assets/lib/bootstrap/css/bootstrap.css"),
        link(rel      := "stylesheet", href     := "/assets/lib/bootstrap/css/main.css"),
        link(rel      := "stylesheet", href     := "/assets/lib/bootstrap/css/chat.css"),
        script(`type` := "text/javascript", src := "/assets/lib/jquery/jquery.js"),
        script(`type` := "text/javascript", src := "/assets/lib/bootstrap/js/bootstrap.js"),
        script(`type` := "text/javascript", src := "/assets/ui-jsdeps.js"),
        script(`type` := "text/javascript", src := "/assets/ui-fastopt.js"),
        // script(`type` := "text/javascript", src := "/assets/ui-opt.js"),
        div(id := targetDiv, style := "position:relative"),
        script(s"JsApp(`$system`,`$url`,'$targetDiv')")
      )
    )
}
