package console.style

import scalacss.Defaults._
import scalacss.defaults.Exports.StyleSheet

class GraphStyles extends StyleSheet.Inline {
  import dsl._
  
  val graph = style(
    width(1200.px),
    height(700.px),
    margin(25.px, auto, 25.px, auto),
    padding(50.px, 50.px, 50.px, 50.px),
    backgroundColor(Color("#ccc"))
  )
}
