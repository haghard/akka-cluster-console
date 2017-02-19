package console.components2

import java.util.concurrent.ThreadLocalRandom

import console.jsGraph.{CgraphNode, GraphNode}
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.forceModule.Force
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.timers.setTimeout

@JSExport
object Graph4 {

  case class NodeLink[N](sourceNode: N, targetNode: N) extends org.singlespaced.d3js.Link[N] {
    override def source = sourceNode
    override def target = targetNode
  }

  type N = CgraphNode
  type Edge = NodeLink[N]

  object Link {
    def apply(x: N, y: N): Edge = NodeLink(x, y)
  }

  //@scala.annotation.tailrec
  //stack unsafe
  def inflate(n: CgraphNode) = {
    def loop[T <: GraphNode](n: T): Unit = {
      if (!js.isUndefined(n.parent)) {
        n.parent.size = n.parent.size + 1
        loop(n.parent)
      } else ()
    }
    loop(n)
  }

  def onClick(n: N) : Unit = {
    println(s"${n.id}  ${n.size}")
    //d3.select(this).select("circle").style("stroke-width", 4)
    //d.fixed = true
  }


  val w: Double = 1024.0
  val h: Double = 840.0
  val p = 10
  val max = 10

  def render(s: Selection[org.scalajs.dom.EventTarget], force: Force[N, Edge]) = {
    force.stop

    s.selectAll[Edge]("line.link")
      .data[Edge](force.links(), { (d: Edge, i: Int) => d.source.id + "-" + d.target.id }).enter()
      .append("line")
      .style("opacity", 1.0)
      .attr("class", "link")
      .style("stroke", "black")
      .style("stroke-width", 1)

    val nodeEnter = s
      .selectAll[N]("q.node").data[N](force.nodes(), { (n: N, i: Int) => n.id.toString }).enter()
      .append("g")
      .attr("class", (n: N) => { s"node ${n.id}"  })
      .call(force.drag())

    nodeEnter.append("circle")
      .attr("r", { (n:N) => if(js.isUndefined(n.parent)) 20 else 5 })
      //{ (n:N) => 10 + n.size })
      .style("fill",  "#2FA02B") //#489dbd // "#2FA02B") //{ (n:N) => if(js.isUndefined(n.parent)) "#2FA02B" else "white" })
      .style("opacity", .9)
      .style("stroke", "black") //
      .style("stroke-miterlimit", 10)
      //.style("stroke-width", "4px")
      .style("stroke-width", 1)
      .on("click", onClick(_))

    nodeEnter
      .insert("image", "text")
      .attr("xlink:href", { (n: N) => if(js.isUndefined(n.parent)) "" else {  if(n.id % 2 == 0) "images/ms.png" else "images/ms2.png" } })
      .attr("width", "60px")
      .attr("height", "40px")

    nodeEnter.append("text")
      .style("text-anchor", "middle")  //start |  middle |  end
      .style("font-family", "Source Sans Pro")
      .style("font-size", "13px")
      .attr("y", 15)
      .text((n: N) => n.host)

    force.start()
  }

  def create(scene: String) = {
    val host0 = Dynamic.literal("id" -> 1.0, "index" -> 1.0, "name" -> "192", "x" -> 50, "y" -> 50,
      "host" -> shared.Network.toInetAddress(178493535), "port" -> 1,
      "roles" -> "ws", "status" -> "up", "size" -> 1).asInstanceOf[N]

    val host1 = Dynamic.literal("id" -> 2.0, "index" -> 1.0, "name" -> "192", "x" -> 50, "y" -> 50,
          "host" -> shared.Network.toInetAddress(178493536), "port" -> 1,
          "roles" -> "ws", "status" -> "up", "size" -> 1).asInstanceOf[N]

    val nodes = Array[N](host0, host1)
    val links = Array[Edge]()

    //generate random graph
    //start with 2 because of root
    (3 to max).foreach { i =>
      val rnd = ThreadLocalRandom.current().nextInt(0, 2 /*nodes.length*/)
      val host = ThreadLocalRandom.current().nextInt()
      val parent: N = nodes(rnd)

      val child = Dynamic.literal("id" -> i, "index" -> i, "name" -> host,
        "x" -> (parent.x + ThreadLocalRandom.current().nextDouble - .15),
        "y" -> (parent.y + ThreadLocalRandom.current().nextDouble - .15),
        "host" -> shared.Network.toInetAddress(host),
        "port" -> 1,
        "roles" -> "ws", "status" -> "up",
        "parent" -> parent, "size" -> 1).asInstanceOf[N]

      inflate(child)
      links.push(Link(parent, child))
      nodes.push(child)
    }

    def tick(e: org.scalajs.dom.Event, s: Selection[org.scalajs.dom.EventTarget]): Unit = {
      nodes.foreach { n =>
        n.x = Math.min(w, Math.max(0, n.x))
        n.y = Math.min(h, Math.max(0, n.y))
      }

      s.selectAll[Edge]("line.link")
        .style("stroke-width", 1)
        .attr("x1", { (link: Edge) => link.source.x })
        .attr("y1", { (link: Edge) => link.source.y })
        .attr("x2", { (link: Edge) => link.target.x })
        .attr("y2", { (link: Edge) => link.target.y })

      s.selectAll[N]("g.node")
        .attr("transform", { (n: N) => "translate(" + n.x + "," + n.y + ")" })
    }

    val svg = d3.select(scene).append("svg")
      .attr("width", w + 2 * p)
      .attr("height", h + 2 * p)

    val force = d3.layout.force[N, Edge]()
      .charge(-400)
      .linkDistance(400)
      .gravity(.3)
      .nodes(nodes)
      .links(links)
      .size((w, h))
      .on("tick", tick(_, svg))
      .on("end", { e: org.scalajs.dom.Event => println("has rendered") })

    render(svg, force)

    ((max + 1)  to (max + 6)).foreach { i =>
      val timeout = (i - max) + 3
      //println(i + " - " + t)
      import scala.concurrent.duration._
      setTimeout(timeout seconds) {
        val rnd = ThreadLocalRandom.current().nextInt(0, 2 /*nodes.length*/)
        val host = ThreadLocalRandom.current().nextInt
        val parent: N = nodes(rnd)

        val child = Dynamic.literal("id" -> i, "index" -> i, "name" -> host,
          "x" -> (parent.x + ThreadLocalRandom.current().nextDouble - .5),
          "y" -> (parent.y + ThreadLocalRandom.current().nextDouble - .5),
          "host" -> shared.Network.toInetAddress(host), "port" -> 1, "roles" -> "ws", "status" -> "up",
          "parent" -> parent, "size" -> 1).asInstanceOf[N]

        inflate(child)
        links.push(Link(parent, child))
        nodes.push(child)
        render(svg, force)
      }
    }
  }

  @JSExport
  def main(): Unit = {
    create("body")
  }
}