package console.components2

import console.jsGraph.CgraphNode
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js._

import scala.scalajs.js._
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.timers.setTimeout

//http://bl.ocks.org/sathomas/11550728
//https://github.com/spaced/scala-js-d3/issues/6
//https://runkit.com/npm/d3-force

@JSExport
object Graph3 {

  case class NodeLink[N](sourceNode: N, targetNode: N) extends org.singlespaced.d3js.Link[N] {
    override def source = sourceNode
    override def target = targetNode
  }

  type Link = NodeLink[CgraphNode]

  object Link {
    def apply(x: CgraphNode, y: CgraphNode): Link = NodeLink(x, y)
  }

  val width  = 800.0
  val height = 600.0

  def create(scene: String) = {
    var nodeSelection: selection.Update[CgraphNode] = null
    var linkSelection: selection.Update[Link]       = null

    def onTick(e: org.scalajs.dom.Event): Unit = {
      nodeSelection
        .attr("cx", { (n: CgraphNode) ⇒
          n.x
        })
        .attr("cy", { (n: CgraphNode) ⇒
          n.y
        })

      linkSelection
        .attr("x1", (n: Link) ⇒ n.source.x)
        .attr("y1", (n: Link) ⇒ n.source.y)
        .attr("x2", (n: Link) ⇒ n.target.x)
        .attr("y2", (n: Link) ⇒ n.target.y)
    }

    val nodes = Array[CgraphNode]()
    val links = Array[Link]()

    val force = d3.layout
      .force[CgraphNode, Link]()
      .nodes(nodes)
      .links(links)
      .charge(-400)
      .linkDistance(200)
      .size((width, height))
      .on("tick", onTick(_))
      .on("end", { e: org.scalajs.dom.Event ⇒
        println("calculations have concluded")
      })

    val svg = d3
      .select(scene)
      .append("svg")
      .attr("width", width)
      .attr("height", height)

    // 1. Add three nodes and three links.
    setTimeout(0) {
      val a = Dynamic
        .literal(
          "id"     → 1.0,
          "index"  → 1.0,
          "name"   → "192",
          "x"      → 50,
          "y"      → 50,
          "host"   → "192",
          "port"   → 1,
          "roles"  → "ws",
          "status" → "up"
        )
        .asInstanceOf[CgraphNode]
      val b = Dynamic
        .literal(
          "id"     → 2.0,
          "index"  → 2.0,
          "name"   → "193",
          "x"      → 60,
          "y"      → 60,
          "host"   → "193",
          "port"   → 2,
          "roles"  → "ws",
          "status" → "up"
        )
        .asInstanceOf[CgraphNode]
      val c = Dynamic
        .literal(
          "id"     → 3.0,
          "index"  → 3.0,
          "name"   → "194",
          "x"      → 70,
          "y"      → 70,
          "host"   → "194",
          "port"   → 3,
          "roles"  → "ws",
          "status" → "up"
        )
        .asInstanceOf[CgraphNode]

      nodes.push(a, b, c)
      links.push(Link(a, b), Link(a, c), Link(b, c))
      render()
    }

    // 2. Remove node B and associated links.
    setTimeout(3000) {
      nodes.splice(1, 1) // remove b
      links.shift()      // remove a-b
      links.pop()        // remove b-c
      render()
    }

    // Add node B back
    setTimeout(6000) {
      val a = nodes(0)
      val b = Dynamic
        .literal(
          "id"     → 2.0,
          "index"  → 2.0,
          "name"   → "193",
          "x"      → 60,
          "y"      → 60,
          "host"   → "193",
          "port"   → 2,
          "roles"  → "ws",
          "status" → "up"
        )
        .asInstanceOf[CgraphNode]
      val c = nodes(1)
      nodes.push(b)
      links.push(Link(a, b), Link(b, c))
      render()
    }

    def render() = {
      linkSelection = svg
        .selectAll[Link](".linkSelection")
        .data(force.links(), { (d: Link, i: Int) ⇒
          d.source.id + "-" + d.target.id
        })

      linkSelection
        .enter()
        .insert("line", ".node")
        .attr("class", "linkSelection")
        .style("stroke", "black")
        .style("stroke-width", 2)

      linkSelection.exit().remove()

      nodeSelection = svg
        .selectAll[CgraphNode](".node")
        .data(force.nodes(), { (d: CgraphNode, i: Int) ⇒
          d.id.toString
        })

      //attr("class", "node")
      nodeSelection
        .enter()
        .append("circle")
        .style("fill", "#2FA02B") //green
        .style("stroke", "black")
        .style("stroke-width", 2)
        .attr("class", (n: CgraphNode) ⇒ "node " + n.id)
        .attr("r", 25)

      nodeSelection.exit().remove()

      force.start()
      println("force.start")
    }
  }

  @JSExport
  def main(): Unit =
    create("body")
}
