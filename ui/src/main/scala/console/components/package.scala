package console

import autowire.ClientProxy
import upickle.Js
import upickle.default._

package object components {
  type Proxy = ClientProxy[shared.ClusterApi, Js.Value, Reader, Writer]

  val jQuery = JQueryStatic
}
