package console

import scala.scalajs.js

package object jsGraph {

  trait GraphNode extends js.Object {
    var id: js.UndefOr[Double]                     = js.undefined
    var index: js.UndefOr[Double]                  = js.undefined
    var name: js.UndefOr[String]                   = js.undefined
    var px: js.UndefOr[Double]                     = js.undefined
    var py: js.UndefOr[Double]                     = js.undefined
    var size: js.UndefOr[Double]                   = js.undefined
    var weight: js.UndefOr[Double]                 = js.undefined
    var x: js.UndefOr[Double]                      = js.undefined
    var y: js.UndefOr[Double]                      = js.undefined
    var subindex: js.UndefOr[Double]               = js.undefined
    var startAngle: js.UndefOr[Double]             = js.undefined
    var endAngle: js.UndefOr[Double]               = js.undefined
    var value: js.UndefOr[Double]                  = js.undefined
    var fixed: js.UndefOr[Boolean]                 = js.undefined
    var children: js.UndefOr[GraphNode]            = js.undefined
    var _children: js.UndefOr[js.Array[GraphNode]] = js.undefined
    var parent: js.UndefOr[GraphNode]              = js.undefined
    var depth: js.UndefOr[Double]                  = js.undefined
  }

  trait GraphLink extends js.Object {
    var source: js.UndefOr[GraphNode] = js.undefined
    var target: js.UndefOr[GraphNode] = js.undefined
  }

  trait ClusterGraphLink extends GraphLink {
    var sourceHost: js.UndefOr[String] = js.undefined
    var targetHost: js.UndefOr[String] = js.undefined
  }

  trait CgraphNode extends GraphNode {
    var host: js.UndefOr[String]   = js.undefined
    var port: js.UndefOr[Int]      = js.undefined
    var roles: js.UndefOr[String]  = js.undefined
    var status: js.UndefOr[String] = js.undefined
  }

  trait AkkaClusterNode extends js.Object {
    var id: js.UndefOr[Double]      = js.undefined
    var x: js.UndefOr[Double]       = js.undefined
    var y: js.UndefOr[Double]       = js.undefined
    var isHost: js.UndefOr[Boolean] = js.undefined
    var host: js.UndefOr[String]    = js.undefined
    var port: js.UndefOr[Int]       = js.undefined
    var roles: js.UndefOr[String]   = js.undefined
    var status: js.UndefOr[String]  = js.undefined
  }

  trait ClusterGraphRoleLink extends ClusterGraphLink {
    var index: js.UndefOr[Int] = js.undefined
  }
}
