package console.components

import console.style.GlobalStyles

object Bootstrap {
  @inline private def bss = GlobalStyles.bootstrapStyles

  // Common Bootstrap contextual styles
  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }
}
