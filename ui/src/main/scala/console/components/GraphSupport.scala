package console.components

import console.jsGraph.{AkkaClusterNode, CgraphNode, GraphNode}

import scala.scalajs.js

trait GraphSupport {

  case class NodeLink[N](sourceNode: N, targetNode: N) extends org.singlespaced.d3js.Link[N] {
    override val source = sourceNode
    override val target = targetNode
  }

  type Vertix = AkkaClusterNode
  type Edge = NodeLink[Vertix]

  object Link {
    def apply(x: Vertix, y: Vertix): Edge = NodeLink(x, y)
  }

  def inflate(n: CgraphNode) = {
    def loop[T <: GraphNode](n: T): Unit = {
      if (!js.isUndefined(n.parent)) {
        n.parent.size = n.parent.size + 1
        loop(n.parent)
      } else ()
    }

    loop(n)
  }
}