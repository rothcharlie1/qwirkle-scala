package player

import spray.json._
import common.Placement

trait Action {

    /*
    All Actions must describe how to serialize them to JSON
    */
    def toJChoice: JsValue
}

object Action {

    def fromJChoice(jchoice: JsValue): Action = jchoice match {
        case JsString("pass") => PassAction
        case JsString("replace") => ExchangeAction
        case JsArray(elements) => {
            val placements: List[Placement] = elements.map(Placement.from1Placement(_)).toList
            PlaceAction(placements)
        }
        case _ => throw new Exception("Provided JChoice was not valid")
    }
}