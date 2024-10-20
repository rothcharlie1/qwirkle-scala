package atomic
import common.PlayerState
import common.Tile
import spray.json._

/*
A structure to encode the publicly-available information about a Player.

@param score A positive integer game score for the player.
@param id A string by which the player is identified.
@throws IllegalArgumentException if score is less than 0.
*/
case class PublicPlayerInfo(score: Int) {
    if (score < 0) throw new IllegalArgumentException("Instantiated PublicPlayerInfo with negative score.")

    def toMockPlayerState(mockAge: Int, mockId: String): PlayerState = {
        new PlayerState(List[Tile](), mockAge, mockId, score)
    }
}

object PublicPlayerInfo {
    
    def fromJson(json: JsValue): PublicPlayerInfo = json match {
        case JsNumber(num) => PublicPlayerInfo(num.intValue)
        case _ =>  throw new IllegalArgumentException("Public player info was not a number score.")
    }
}