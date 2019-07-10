package console.components

import scalajs.concurrent.JSExecutionContext.Implicits.queue
import upickle.default._
import upickle.Js
import org.scalajs.dom
import scala.concurrent.Future

object AjaxClient extends autowire.Client[Js.Value, Reader, Writer] {

  override def doCall(req: Request): Future[Js.Value] = {
    println("path:" + req.path.mkString("/") + "\n args:" + req.args.toSeq.mkString(","))
    val map = req.args.foldLeft(scala.collection.mutable.LinkedHashMap[String, Js.Value]()) { (acc, c) ⇒
      acc.+=(c._1 → c._2)
    }

    dom.ext.Ajax
      .post(
        url = shared.Routes.pref + "/" + req.path.mkString("/"),
        data = upickle.json.write(Js.Obj(map))
      )
      .map(_.responseText)
      .map(s ⇒ upickle.json.read(s))
  }

  def read[Result: Reader](p: Js.Value) = readJs[Result](p)

  def write[Result: Writer](r: Result) = writeJs(r)
}
