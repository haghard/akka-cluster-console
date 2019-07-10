package console.scripts

import scalatags.Text.all._

object AppScript {
  def apply() =
    html(
      head(
        link(rel := "stylesheet", href := "/assets/lib/bootstrap/css/bootstrap.css")
        //link(rel := "stylesheet", href := "http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css")
      ),
      body(
        script(`type` := "text/javascript", src := "/assets/lib/jquery/jquery.js"),
        script(`type` := "text/javascript", src := "/assets/lib/bootstrap/js/bootstrap.js"),
        //script(`type` := "text/javascript", src := "/assets/lib/bootstrap/js/d3.v3.min.js"),
        script(`type` := "text/javascript", src := "/assets/lib/bootstrap/js/d3.v4.min.js"),
        script(`type` := "text/javascript", src := "/assets/ui-jsdeps.min.js"),
        script(`type` := "text/javascript", src := "/assets/ui-opt.js"),
        script(`type` := "text/javascript", src := "/assets/ui-launcher.js"),
        div(id := "scene"),
        script(s"console.JsAppModule().main()")
        //script("console.components.Graph4().main()")
      )
    )
}
