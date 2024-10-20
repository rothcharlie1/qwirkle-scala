package player

import common.Placement
import spray.json._

case class PlaceAction(placements: List[Placement]) extends Action {

    override def toJChoice: JsValue = JsArray(placements.map(_.to1Placement))
}