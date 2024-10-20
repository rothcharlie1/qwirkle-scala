package common

import atomic.Posn
import spray.json._

/*
Represents a placement of a single tile at specific position.
*/
case class Placement(pos: Posn, tile: Tile) {
    
    def to1Placement: JsValue = JsObject(Map(
        "coordinate" -> pos.toJCoordinate,
        "1tile" -> tile.toJTile
    ))
}

object Placement {

    def from1Placement(json: JsValue): Placement = json match {
        case JsObject(fields) => {
            fields.get("coordinate") match {
                case Some(jcoordinate) => {
                    fields.get("1tile") match {
                        case Some(jtile) => Placement(Posn.fromJCoordinate(jcoordinate), Tile.fromJTile(jtile))
                        case None => throw new IllegalArgumentException("1Placement did not contain field '1tile'.")
                    }
                }
                case None => throw new IllegalArgumentException("1Placement did not contain field 'coordinate'.")
            }
        }
        case _ => throw new IllegalArgumentException("1Placement was not a JSON Object.")
    }
}