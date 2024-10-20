package common

import scala.collection.mutable.{Map => MutableMap}
import atomic.PrivatePlayerInfo
import atomic.PublicPlayerInfo
import atomic.Posn
import spray.json._

/*
Represents the information given to a player at the start of its turn.

@param player A PrivatePlayerInfo object containing information private to the active player.
@param opponents A List of PublicPlayerInfo containing public information about all players, 
sorted in turn order (where opponents[0] is the active player)
@param tilesRemaining A positive integer representing the number of tiles that have not been 
placed on the board yet.
@throws IllegalArgumentException if tilesRemaining is less than 0.
*/
case class TurnInfo(player: PrivatePlayerInfo, opponents: List[PublicPlayerInfo], tilesRemaining: Int, map: MapMap) {
    if (tilesRemaining < 0) throw new IllegalArgumentException("Instantiated TurnInfo with less than 0 tiles remaining.")

    def toPrivateGameState: GameState = {
        var players: List[PlayerState] = player.toMockPlayerState(opponents.size + 1) :: Nil

        var age: Int = opponents.size
        for (pubPlayer <- opponents) {
            players = pubPlayer.toMockPlayerState(age, s"opponent age $age") :: players
        }
        new GameState(map, players)
    }

    def toJPub: JsValue = {
        val jmap = map.toJMap
        val tiles = JsNumber(tilesRemaining)
        val jplayer = player.toJPlayer
        val jopponents = opponents.map(opp => JsNumber(opp.score)).toList
        JsObject(Map(
            "map" -> jmap,
            "tile*" -> tiles,
            "players" -> JsArray(jplayer :: jopponents)
        ))
    }
}

object TurnInfo {

    def fromJPub(jpub: JsValue): TurnInfo = jpub match {
        case JsObject(fields) => {
            fields.get("tile*") match {
                case Some(JsNumber(tilesRemaining)) => {
                    fields.get("map") match {
                        case Some(mapJs) => {
                            val map = MapMap.fromJMap(mapJs).getMap
                            fields.get("players") match {
                                case Some(JsArray(players)) => {
                                    val privatePlayer = PrivatePlayerInfo.fromJPlayer(players.head)
                                    val publicPlayers = players.tail.map(PublicPlayerInfo.fromJson(_))
                                    TurnInfo(privatePlayer, publicPlayers.toList, tilesRemaining.toInt, new MapMap(map.toMap))
                                }
                                case Some(_) => throw new IllegalArgumentException("JPub 'players' field was not an array.")
                                case None => throw new IllegalArgumentException("JPub did not contain field 'players'.")
                            }
                        }
                        case None => throw new IllegalArgumentException("JPub does not contain field 'map'.")
                    }
                }
                case Some(_) => throw new IllegalArgumentException("JPub 'tile*' field was not a JSON number.")
                case None => throw new IllegalArgumentException("JPub did not contain field 'tile*'.")
            }
        }
        case _ => throw new IllegalArgumentException("JPub was not a JsObject.")
    }
}