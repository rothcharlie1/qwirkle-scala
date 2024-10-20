package player

import spray.json._

case object PassAction extends Action {

    override def toJChoice: JsValue = JsString("pass")
}