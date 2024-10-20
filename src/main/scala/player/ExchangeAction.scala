package player

import common.Tile
import spray.json._

object ExchangeAction extends Action {

    override def toJChoice: JsValue = JsString("replace")
}