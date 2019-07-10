package console

import scala.scalajs.js

package object jsGraph {

  trait GraphNode extends js.Object {
    var id: Double                     = js.native
    var index: Double                  = js.native
    var name: String                   = js.native
    var px: Double                     = js.native
    var py: Double                     = js.native
    var size: Double                   = js.native
    var weight: Double                 = js.native
    var x: Double                      = js.native
    var y: Double                      = js.native
    var subindex: Double               = js.native
    var startAngle: Double             = js.native
    var endAngle: Double               = js.native
    var value: Double                  = js.native
    var fixed: Boolean                 = js.native
    var children: js.Array[GraphNode]  = js.native
    var _children: js.Array[GraphNode] = js.native
    var parent: GraphNode              = js.native
    var depth: Double                  = js.native
  }

  trait GraphLink extends js.Object {
    var source: GraphNode = js.native
    var target: GraphNode = js.native
  }

  trait ClusterGraphLink extends GraphLink {
    var sourceHost: String = js.native
    var targetHost: String = js.native
  }

  trait CgraphNode extends GraphNode {
    var host: String   = js.native
    var port: Int      = js.native
    var roles: String  = js.native
    var status: String = js.native
  }

  trait AkkaClusterNode extends js.Object {
    var id: Double      = js.native
    var x: Double       = js.native
    var y: Double       = js.native
    var isHost: Boolean = js.native
    var host: String    = js.native
    var port: Int       = js.native
    var roles: String   = js.native
    var status: String  = js.native
  }

  trait ClusterGraphRoleLink extends ClusterGraphLink {
    var index: Int = js.native
  }
}
