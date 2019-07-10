package console.components

import java.util.concurrent.ThreadLocalRandom

import autowire.{ClientProxy, _}
import japgolly.scalajs.react
import japgolly.scalajs.react.{BackendScope, CallbackTo}
import org.scalajs.dom
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Selection, d3, forceModule}
import upickle.Js
import upickle.default._

import scala.scalajs.js
import scala.scalajs.js.{Array, Dynamic}
import scala.util.control.NonFatal
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import Dynamic.{literal ⇒ lit}

object GraphModule extends GraphSupport {

  case class GraphProps(system: String, proxy: ClientProxy[shared.ClusterApi, Js.Value, Reader, Writer])

  case class GraphState(
    system: Option[String] = None,
    nodes: Array[Vertix] = Array[Vertix](),
    links: Array[Edge] = Array[Edge](),
    selection: Option[Selection[org.scalajs.dom.EventTarget]] = None,
    force: Option[forceModule.Force[Vertix, Edge]] = None
  )

  class GraphBackend(scope: BackendScope[GraphProps, GraphState]) {
    val quotes = Vector(
      "The natural state in a distributed system is partial order.#Distributed systems for fun and profit",
      "If a tree falls in a forest and no one is around to hear it, does it make a sound ?#Philosopher George Berkeley",
      "Linearizable consistency: Under linearizable consistency, all operations appear to have executed atomically in an order that is consistent with the global real-time ordering of operations.#Herlihy & Wing, 1991",
      "Sequential consistency: Under sequential consistency, all operations appear to have executed atomically in some order that is consistent with the order seen at individual nodes and that is equal at all nodes.#Lamport, 1979"
    )

    var interval: js.UndefOr[js.timers.SetIntervalHandle] = js.undefined

    val p         = 10
    val svgWidth  = 1200
    val svgHeight = 700

    val tooltip = d3
      .select("body") //body
      .append("div")
      .attr("class", "tooltip")
      .style("opacity", 0)
      .style("position", "absolute")
      .style("text-align", "left")
      .style("width", "150px")
      .style("height", "80px")
      .style("padding", "2px")
      .style("font", "12px sans-serif")
      .style("background", "lightblue") //lightsteelblue #226d9e  lightgray
      .style("border", "1px")
      .style("border-radius", "8px")
      .style("pointer-events", "none")

    def tick =
      scope.modState { prev ⇒
        val nextForce = prev.force.map { f ⇒
          f.stop()
          val nodes            = f.nodes()
          val links            = f.links()
          val influentialNodes = nodes.filter(_.id < (nodes.length - 1))
          val influentialLinks = links.filter { l ⇒
            nodes.indexOf(l.sourceNode) > -1 && nodes.indexOf(l.targetNode) > -1
          }
          println("nodes.length:  " + influentialNodes.length)
          println("nodes.length:  " + influentialLinks.length)
          onDelete(prev.selection.get, f, influentialNodes, influentialLinks)
        }
        prev.copy(force = nextForce)
      }

    def start = Callback {
      //to delete links for a search session
      interval = js.timers.setInterval(5000)(tick.runNow())
    }

    def clear = Callback {
      interval foreach js.timers.clearInterval
      interval = js.undefined
    }

    def render(): VdomElement =
      scope.state
        .map { s ⇒
          s.system.fold(<.div()) { system ⇒
            val q              = quotes(ThreadLocalRandom.current.nextInt(quotes.length))
            val quoteAndAuthor = q.split("#")
            <.div(
              <.aside(
                <.header("A quote of the day"),
                <.blockquote(
                  ^.cls := "quote",
                  <.p(quoteAndAuthor(0)),
                  <.footer(quoteAndAuthor(1))
                )
              ),
              <.h4(s"System name: $system")
            )
          }
        }
        .runNow()

    def fetchClusterProfile(props: GraphProps): CallbackTo[Unit] = {
      import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
      react.Callback.future {
        props.proxy
          .clusterProfile()
          .call()
          .map { cProfile ⇒
            dom.console.log("fetchClusterProfile: " + cProfile.toString)

            //cProfile

            val location = 10
            val hosts = cProfile.members
              .map(_.address.host)
              .zipWithIndex
              .map {
                case (address, i) ⇒
                  lit(
                    "id"     → i,
                    "x"      → (location * (i + 1)),
                    "y"      → 40,
                    "isHost" → true,
                    "host"   → address,
                    "port"   → 0,
                    "roles"  → "host",
                    "status" → ""
                  ).asInstanceOf[Vertix]
              }

            val members = cProfile.members.zipWithIndex.map {
              case (m, ind) ⇒
                lit(
                  "id"     → (hosts.size + ind).toString,
                  "x"      → 50,
                  "y"      → 50,
                  "isHost" → false,
                  "host"   → m.address.host,
                  "port"   → m.address.port,
                  "roles"  → m.roles.mkString(","),
                  "status" → m.state.toString
                ).asInstanceOf[Vertix]
            }

            val vertices: js.Array[Vertix] = (members ++ hosts).toJsArray
            val edges: js.Array[Edge] = hosts
              .flatMap(r ⇒ members.filter(_.host == r.host).map(h ⇒ Link(r, h)))
              .toJsArray

            val svg = d3
              .select("body")
              .append("svg")
              .attr("width", svgWidth)
              .attr("height", svgHeight)
              .style("margin", "25px auto 25px 25px")
              .style("padding", "50px 50px 50px 50px")
              .style("background-color", "white")
              .style("box-shadow", "0px 0px 20px #ccc")

            val force = d3.layout
              .force[Vertix, Edge]()
              .charge(-400)
              .alpha(0.2)
              .linkDistance(350)
              .gravity(.5)
              .nodes(vertices)
              .links(edges)
              .size((svgWidth - 5.0, svgHeight - 5.0))
              .on("tick", { (e: org.scalajs.dom.Event) ⇒
                onTick(e, svg)
              })

            scope.modState { state ⇒
              onChange(svg, force)
              state.copy(Option(cProfile.system), vertices, edges, Option(svg), Option(force.start))
            } //>> start
          }
          .recover {
            case _: org.scalajs.dom.ext.AjaxException ⇒
              //println("Ajax call error")
              scope.modState(s ⇒ s.copy(system = Option("cluster-profile error")))
            case NonFatal(e) ⇒
              //println(e.getMessage + " error")
              scope.modState(s ⇒ s.copy(system = Option(s"Unexpected cluster-profile error: ${e.getMessage}")))
          }
      }
    }

    def onDelete(
      s: Selection[org.scalajs.dom.EventTarget],
      force: forceModule.Force[Vertix, Edge],
      newNodes: js.Array[Vertix],
      newLinks: js.Array[Edge]
    ): forceModule.Force[Vertix, Edge] = {
      println("node length" + force.nodes().length)
      s.selectAll[Vertix]("q.node")
        .data[Vertix](newNodes, { (n: Vertix, i: Int) ⇒
          n.id.toString
        })
        .exit()
        .transition()
        .duration(1000)
        .style("opacity", 0)
        .remove()

      s.selectAll[Edge]("line.link")
        .data[Edge](newLinks, { (d: Edge, i: Int) ⇒
          d.source.id + "-" + d.target.id
        })
        .exit()
        .transition()
        .duration(1000)
        .style("opacity", 0)
        .remove()

      force
        .nodes(newNodes)
        .links(newLinks)
      force.start()
    }

    /*
   def onChange2(s: Selection[org.scalajs.dom.EventTarget], force: forceModule.Force[Vertix, Edge]): Unit = {

      s.selectAll[Edge]("line.link")
        .data[Edge](force.links(), { (d: Edge, i: Int) => d.source.id + "-" + d.target.id }).enter()
        .append("line")
        .style("opacity", 1.0)
        .attr("class", "link")
        .style("stroke", "black")
        .style("stroke-width", 1)

      val nodeEnter = s.selectAll[Vertix]("q.node")
        .data[Vertix](force.nodes(), { (n: Vertix, i: Int) => n.id.toString }).enter()
        .append("g")
        .attr("class", { (n: Vertix) => s"node ${n.id}" })
        .call(force.drag())
      /*
            nodeEnter
              .append("circle")
              .attr("r", 20)
              .style("fill", "none")
              .style("stroke", "#489dbd")
              .style("stroke-miterlimit", 10)
              .style("stroke-width", "4px")
              .on("click", onClick(_))
              .on("mouseover", onMouseover(_))
              .on("mouseout", onMouseout(_))
     */

      nodeEnter
        .append("text")
        .style("text-anchor", "middle")
        .style("font-family", "Source Sans Pro")
        .style("font-size", "12px")
        .attr("y", 15)
        .text((n: Vertix) => n.host)

      nodeEnter.insert("image", "text")
        .attr("xlink:href", "images/fig1.png")
        .attr("width", "60px")
        .attr("height", "40px")
        .on("click", onClick(_))
        .on("mouseover", onMouseover(_))
        .on("mouseout", onMouseout(_))
    }
     */

    def onChange(s: Selection[org.scalajs.dom.EventTarget], force: forceModule.Force[Vertix, Edge]): Unit = {
      s.selectAll[Edge]("line.link")
        .data[Edge](force.links(), { (d: Edge, i: Int) ⇒
          d.source.id + "-" + d.target.id
        })
        .enter()
        .append("line")
        .style("opacity", 1.0)
        .attr("class", "link")
        .style("stroke", "black")
        .style("stroke-width", 1)

      val nodeEnter = s
        .selectAll[Vertix]("q.node")
        .data[Vertix](force.nodes(), { (n: Vertix, i: Int) ⇒
          n.id.toString
        })
        .enter()
        .append("g")
        .attr("class", { (n: Vertix) ⇒
          s"node ${n.id}"
        })
        .call(force.drag())

      nodeEnter
        .append("circle")
        .attr("r", { (n: Vertix) ⇒
          if (n.isHost) 40 else 25
        })
        .style("fill", { (n: Vertix) ⇒
          if (n.isHost) "#2FA02B" else "#489dbd"
        })
        .style("opacity", .9)
        .style("stroke", "black")
        .style("stroke-miterlimit", 10)
        .style("stroke-width", 1)
        .on("click", onClick(_))
        .on("mouseover", onMouseover(_))
        .on("mouseout", onMouseout(_))

      /*
            nodeEnter.insert("image", "text")
              .attr("xlink:href", { (n: Vertix) =>
                if (js.isUndefined(n.parent)) "" else {
                  if (n.id % 2 == 0) "images/ms.png" else "images/ms2.png"
                }
              })
              .attr("width", "60px")
              .attr("height", "40px")
       */

      nodeEnter
        .append("text")
        .style("text-anchor", "middle") //start |  middle |  end
        .style("font-family", "Source Sans Pro")
        .style("font-size", "13px")
        .attr("y", 15)
        .text((n: Vertix) ⇒ if (n.isHost) n.host else s"${n.roles}@${n.host}:${n.port}")
    }

    def onMouseout(n: Vertix): Unit =
      tooltip
        .transition()
        .duration(500)
        .style("opacity", 0)

    def onMouseover(n: Vertix): Unit = {
      tooltip.transition().duration(200).style("opacity", .9)
      tooltip
        .html(s"id: ${n.id} <br/>\naddress: ${n.host}:${n.port} <br/>\n role: ${n.roles}")
        .style("left", n.x + "px")
        .style("top", (n.y - 20) + "px")
    }

    def onClick(n: Vertix): Unit = println(s"${n.id}")

    def onTick(e: org.scalajs.dom.Event, s: Selection[org.scalajs.dom.EventTarget]): Unit = {
      s.selectAll[Edge]("line.link")
        .style("stroke-width", 1)
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

      s.selectAll[Vertix]("g.node")
        .attr("transform", { (n: Vertix) ⇒
          "translate(" + n.x + "," + n.y + ")"
        })
    }
  }

  private val component = ScalaComponent
    .builder[GraphProps]("GraphComponent")
    .initialState(GraphState())
    .backend(new GraphBackend(_))
    .renderPS { (scope, props, state) ⇒
      scope.backend.render()
    }
    .componentDidMount(scope ⇒ scope.backend.fetchClusterProfile(scope.props))
    .componentWillUnmount { scope ⇒
      CallbackTo {
        //remove svg
        scope.state.force.foreach(_.stop)
        val b   = dom.document.getElementsByTagName("body")
        val svg = dom.document.getElementsByTagName("svg")
        b(0).removeChild(svg(0))
      }
    }
    .build

  def apply(system: String, proxy: ClientProxy[shared.ClusterApi, Js.Value, Reader, Writer]) =
    component(GraphProps(system, proxy))
}
