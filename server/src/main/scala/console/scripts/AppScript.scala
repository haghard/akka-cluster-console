package console.scripts

import scalatags.Text.all._

object AppScript {
  def apply(psw: String) =
    html(
      head(
        link(rel := "stylesheet", href := "/console-assets/lib/bootstrap/css/bootstrap.css")
        // link(rel := "stylesheet", href := "http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css")
      ),
      body(
        script(`type` := "text/javascript", src := "/console-assets/lib/jquery/jquery.js"),
        script(`type` := "text/javascript", src := "/console-assets/lib/bootstrap/js/bootstrap.js"),
        script(`type` := "text/javascript", src := "/console-assets/ui-jsdeps.min.js"),
        script(`type` := "text/javascript", src := "/console-assets/ui-opt.js"),
        // script(`type` := "text/javascript", src := "/console-assets/ui-launcher.js"),
        div(id := "scene"),
        script(s"console.JsApplication().main('$psw')")
      )
    )
}
