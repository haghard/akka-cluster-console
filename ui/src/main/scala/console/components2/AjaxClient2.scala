package console.components2

import java.nio.ByteBuffer

import boopickle.Default.{Pickler, _}
import org.scalajs.dom

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.typedarray._

object AjaxClient2 extends autowire.Client[ByteBuffer, Pickler, Pickler] {
  override def doCall(req: Request): Future[ByteBuffer] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = Pickle.intoBytes(req.args),
      responseType = "arraybuffer",
      headers = Map("Content-Type" -> "application/octet-stream")
    ).map(r => TypedArrayBuffer.wrap(r.response.asInstanceOf[ArrayBuffer]))
  }

  override def read[Result: Pickler](p: ByteBuffer) = Unpickle[Result].fromBytes(p)
  override def write[Result: Pickler](r: Result) = Pickle.intoBytes(r)
}