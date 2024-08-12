package console.components

import console.jsGraph.AkkaClusterNode

object GraphSupport {

  final case class NodeLink[N](sourceNode: N, targetNode: N) extends org.singlespaced.d3js.Link[N] {
    override val source = sourceNode
    override val target = targetNode
  }
}

trait GraphSupport {

  type Vertix = AkkaClusterNode
  type Edge   = GraphSupport.NodeLink[Vertix]

  object Link {
    def apply(x: Vertix, y: Vertix): Edge = GraphSupport.NodeLink(x, y)
  }
}
