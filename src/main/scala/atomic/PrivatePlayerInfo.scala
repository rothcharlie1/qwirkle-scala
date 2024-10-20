package atomic
import common.PlayerState
import common.Tile
import spray.json._

/*
A structure to pass information private to the active player to the referee and player.

@param tiles The Tiles the player currently holds.
*/
case class PrivatePlayerInfo(tiles: List[Tile], score: Int, id: String) {
    def toMockPlayerState(mockAge: Int): PlayerState = 
        new PlayerState(tiles, mockAge, id, score)

    def toJPlayer: JsValue = {
        val jscore = JsNumber(score)
        val jname = JsString(id)
        val jtiles = JsArray(tiles.map(_.toJTile))
        JsObject(Map(
            "score" -> jscore,
            "name" -> jname,
            "tile*" -> jtiles
        ))
    }
}

object PrivatePlayerInfo {

    def fromJPlayer(jplayer: JsValue): PrivatePlayerInfo = jplayer match {
        case JsObject(fields) => {
            fields.get("tile*") match {
                case Some(JsArray(tiles)) => {
                    val deserializedTiles: List[Tile] = tiles.map(Tile.fromJTile(_)).toList
                    fields.get("score") match {
                        case Some(JsNumber(score)) => fields.get("name") match {
                            case Some(JsString(value)) => return PrivatePlayerInfo(deserializedTiles, score.toInt, value)
                            case _ => throw new IllegalArgumentException("JPlayer 'name' field was not formatted correctly.")
                        } 
                        case Some(_) => throw new IllegalArgumentException("JPlayer 'score' field was not a JSON Number.")
                        case None => throw new IllegalArgumentException("JPlayer does not contain 'score' field.")
                    }
                }
                case Some(_) => throw new IllegalArgumentException("JPlayer 'tile*' field was not a JSON Array.")
                case None => throw new IllegalArgumentException("JPlayer did not contain field 'tile*'.")
            }
        }
        case _ => throw new IllegalArgumentException("Provided JPlayer was not a JSON Object.")
    }
}