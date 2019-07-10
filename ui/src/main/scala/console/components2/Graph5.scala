package console.components2

import java.util.concurrent.ThreadLocalRandom

import console.jsGraph.{CgraphNode, GraphNode}
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.annotation.JSExport
@JSExport
object Graph5 {

  case class NodeLink[N](sourceNode: N, targetNode: N) extends org.singlespaced.d3js.Link[N] {
    override def source = sourceNode

    override def target = targetNode
  }

  type N    = CgraphNode
  type Edge = NodeLink[N]

  object Link {
    def apply(x: N, y: N): Edge = NodeLink(x, y)
  }

  val w: Double = 1024.0
  val h: Double = 840.0
  val p         = 10
  val max       = 50

  def inflate(n: CgraphNode) = {
    def loop[T <: GraphNode](n: T): Unit =
      if (!js.isUndefined(n.parent)) {
        n.parent.size = n.parent.size + 1
        loop(n.parent)
      } else ()

    loop(n)
  }

  def create(scene: String) = {
    var nodeId: Double = 1.0
    val root = Dynamic
      .literal(
        "id"     → 1.0,
        "index"  → 1.0,
        "name"   → "192",
        "x"      → 50,
        "y"      → 50,
        "host"   → "192",
        "port"   → 1,
        "roles"  → "ws",
        "status" → "up",
        "size"   → 1
      )
      .asInstanceOf[N]

    nodeId = nodeId + 1
    val nodes = Array[N](root)
    val links = Array[Edge]()

    def onTick(e: org.scalajs.dom.Event, s: Selection[org.scalajs.dom.EventTarget]): Unit = {
      nodes.foreach { n ⇒
        n.x = Math.min(w, Math.max(0, n.x))
        n.y = Math.min(h, Math.max(0, n.y))
      }

      s.selectAll[Edge](".link")
        //.style("stroke-width", { (link: Edge) => Math.sqrt(link.target.size) })
        .attr("x1", { (link: Edge) ⇒
          link.source.x
        })
        .attr("y1", { (link: Edge) ⇒
          link.source.y
        })
        .attr("x2", { (link: Edge) ⇒
          link.target.x
        })
        .attr("y2", { (link: Edge) ⇒
          link.target.y
        })

      s.selectAll[N](".node")
        .attr("r", { (n: N) ⇒
          10 + (n.size)
        }) //if you have a big number of nodes /*Math.sqrt(n.size)*/
        .attr("cx", { (n: N) ⇒
          n.x
        })
        .attr("cy", { (n: N) ⇒
          n.y
        })
    }

    val svg = d3
      .select(scene)
      .append("svg")
      .attr("width", w + 2 * p)
      .attr("height", h + 2 * p)

    val force = d3.layout
      .force[N, Edge]()
      .charge(-400)
      .linkDistance(100)
      .nodes(nodes)
      .links(links)
      .size((w, h))
      .on("tick", onTick(_, svg))

    val num   = 5
    val iterN = max / num

    (1 to num).foreach { i ⇒
      d3.timer(
        () ⇒ {
          val rnd       = ThreadLocalRandom.current().nextInt(0, nodes.length)
          val host      = ThreadLocalRandom.current().nextInt(0, 1000)
          val parent: N = nodes(rnd)

          val child = Dynamic
            .literal(
              "id"     → nodeId,
              "index"  → nodeId,
              "name"   → host,
              "x"      → (parent.x + ThreadLocalRandom.current().nextDouble - .5),
              "y"      → (parent.y + ThreadLocalRandom.current().nextDouble - .5),
              "host"   → host,
              "port"   → 1,
              "roles"  → "ws",
              "status" → "up",
              "parent" → parent,
              "size"   → 1
            )
            .asInstanceOf[N]

          inflate(child)
          links.push(Link(parent, child))
          nodes.push(child)
          nodeId = nodeId + 1

          val nodeUpdate = svg
            .selectAll[N](".node")
            .data[N](force.nodes(), { (d: N, i: Int) ⇒
              d.id.toString
            })

          nodeUpdate
            .enter()
            .append("circle")
            .style("fill", "#2FA02B")
            .style("stroke", "black")
            .style("stroke-width", 2)
            .style("opacity", 1.0)
            .attr("class", (n: N) ⇒ s"node ${n.id}")
            .attr("cx", { (n: N) ⇒
              n.x
            })
            .attr("cy", { (n: N) ⇒
              n.y
            })
            //.on("click", onClick(_))
            .call(force.drag)

          val linkUpdate = svg
            .selectAll[Edge](".link")
            .data[Edge](force.links(), { (d: Edge, i: Int) ⇒
              d.source.id + "-" + d.target.id
            })

          val linkEnter = linkUpdate.enter()

          linkEnter
            .insert("line", ".node")
            .style("opacity", .5)
            .attr("class", "link")
            .style("stroke", "black")
            .style("stroke-width", 2)

          force.start()
          val stop = nodes.length >= iterN * i

          if (stop) {
            println(s"stop iter №$i Nodes size: ${iterN * i}")
          }
          stop
        },
        delay = 2000.0
      )
    }

  }

  @JSExport
  def main(): Unit =
    create("body")
}
