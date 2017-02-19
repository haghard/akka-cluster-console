package console.components

import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import upickle.default._
import upickle.Js
import autowire._
import org.scalajs.dom
import scala.concurrent.Future


object AjaxClient extends autowire.Client[Js.Value, Reader, Writer]{
  override def doCall(req: Request): Future[Js.Value] = {
    println("path:" + req.path.mkString("/") + "\n args:" + req.args.toSeq.mkString(","))
    dom.ext.Ajax.post(
      url = shared.Routes.pref + "/" + req.path.mkString("/"),
      data = upickle.json.write(Js.Obj(req.args.toSeq:_*))
    ).map(_.responseText)
     .map(upickle.json.read)
  }

  def read[Result: Reader](p: Js.Value) = readJs[Result](p)
  def write[Result: Writer](r: Result) = writeJs(r)
}
