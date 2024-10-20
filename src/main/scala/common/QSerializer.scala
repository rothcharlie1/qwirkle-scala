package common

import scala.io.StdIn.readLine
import spray.json._
import scala.collection.mutable.{Map => MutMap}
import atomic.Shape.Shape
import atomic.Color.Color
import atomic.Color
import atomic.Shape
import atomic.PrivatePlayerInfo
import atomic.PublicPlayerInfo
import atomic.Posn
import player.strategy.DAGStrategy
import player.strategy.Strategy
import player.strategy.LDASGStrategy
import player.Player
import player.StrategyPlayer
import player.ExceptionPlayer
import player.LoopPlayer
import player.strategy.CheatStrategyFactory
import referee.GameResult

object QSerializer {

    def deserializeJState(json: JsValue): GameState = json match {
        case JsObject(fields) => {
            val map = deserializeJMap(fields.get("map").get)
            var playerStates: List[PlayerState] = Nil
            fields.get("players") match {
                case Some(JsArray(elements)) => {
                    for (player <- elements) {
                        playerStates = deserializeJPlayer(player).toMockPlayerState(1) :: playerStates
                    }
                }
                case _ => throw new IllegalArgumentException("JState 'players' is incorrect.")
            }
            val tiles = fields.get("tile*") match {
                case Some(JsArray(elements)) => elements.map(deserializeJTile(_)).toList
                case _ => throw new IllegalArgumentException("JState 'tile*' field is incorrect.")
            }
            return new GameState(map, playerStates.reverse, tiles)
        }
        case _ => throw new IllegalArgumentException("JState was not a JSON object.")
    }

    def deserializeJActors(json: JsValue): List[Player] = json match {
        case JsArray(elements) => elements.map(deserializeJActorSpec(_)).toList
        case _ => throw new IllegalArgumentException("JActors was not a JSON array")
    }

    def serializeGameResult(result: GameResult): JsArray = {
        val winners = JsArray(result.winners.sorted.map(JsString(_)))
        val misbehaved = JsArray(result.misbehaved.map(JsString(_)))
        JsArray(List(winners, misbehaved))
    }

    def deserializeJActorSpec(json: JsValue): Player = json match {
        case JsArray(elements) if elements.size == 2 => {
            val name: String = elements(0) match {
                case JsString(value) => value
                case _ => ""
            }
            val strat = deserializeJStrategy(elements(1))
            return new StrategyPlayer(name, strat)
        }
        case JsArray(elements) if elements.size == 3 => {
            val name: String = elements(0) match {
                case JsString(value) => value
                case _ => ""
            }
            val strat = deserializeJStrategy(elements(1))
            val exnMethod: String = elements(2) match {
                case JsString(value) => value
                case _ => ""
            }
            return new ExceptionPlayer(name, strat, exnMethod)
        }
        case JsArray(elements) if elements.size == 4 => {
            val name: String = elements(0) match {
                case JsString(value) => value
                case _ => ""
            }
            val strat = deserializeJStrategy(elements(1))
            elements(2) match {
                case JsString("a cheat") => elements(3) match {
                    case JsString(value) => return new StrategyPlayer(name, CheatStrategyFactory.create(value, strat))
                    case _ => throw new IllegalArgumentException("JCheat was not a string")
                }
                case JsString(jexn) => elements(3) match {
                    case JsNumber(count) => {
                        return new LoopPlayer(name, strat, jexn, count.intValue)
                    }
                    case _ => throw new IllegalArgumentException("Fourth argument to exception player was not a number")
                }
                case _ => throw new IllegalArgumentException("Third argument in a JActorSpec was neither 'a cheat' nor an exception string.")
            }
            
        }
        case _ => throw new IllegalArgumentException("JActorSpec was not a 2 or 3 length array.")
    }

    def deserializeJMap(json: JsValue): MapMap = {
        var grid: MutMap[Posn, Tile] = MutMap[Posn, Tile]()
        json match {
            case JsArray(elements) => elements.map(jrow => deserializeJRow(jrow, grid))
            case _ => throw new IllegalArgumentException("JMap was not a JSON array.")
        }
        return new MapMap(grid.toMap)
    }

    def serializeJMap(jmap: Map[Posn, Tile]): JsArray = {
        var rowLists: MutMap[Int, List[List[JsValue]]] = MutMap[Int, List[List[JsValue]]]()
        for ((posn, tile) <- jmap) {
            var jcell = List(JsNumber(posn.x), serializeJTile(tile))
            rowLists.get(-1 * posn.y) match {
                case Some(elements) => {
                    rowLists(-1 * posn.y) = jcell :: rowLists(-1 * posn.y)
                }
                case None => rowLists(-1 * posn.y) = jcell :: Nil
            }
        }
        var jrows: List[JsArray] = Nil
        for ((rowIdx, jcells) <- rowLists) {
            jrows = JsArray(JsNumber(rowIdx) :: jcells.map(JsArray(_))) :: jrows
        }
        JsArray(jrows)
    }
    
    def serializeJTile(tile: Tile): JsObject = {
        JsObject(Map("color" -> JsString(tile.color.toString), "shape" -> JsString(tile.shape.toString)))
    }

    def serializePositionsToJsArray(posns: List[Posn]): JsArray = {
        return JsArray(serializePositionsToList(posns))
    }

    private def serializePositionsToList(posns: List[Posn]): List[JsObject] = {
        posns match {
            case head :: tail => serializePosnToJCoordinate(head) :: serializePositionsToList(tail)
            case Nil => Nil
        }
    }

    def serializePosnToJCoordinate(pos: Posn): JsObject = {
        return JsObject(Map("row" -> JsNumber(-1*pos.y), "column" -> JsNumber(pos.x)))
    }

    def deserializeJRow(jrow: JsValue, grid: MutMap[Posn, Tile]): Unit = {
        jrow match {
            case JsArray(elements) => {
                elements.head match {
                    case JsNumber(rowIdx) => {
                        elements.tail.map(jcell => deserializeJCell(jcell, grid, rowIdx.toInt))
                    }
                    case _ => throw new IllegalArgumentException("First element of JRow was not an Integer.")
                }
            }
            case _ => throw new IllegalArgumentException("JRow was not a JSON array.")
        }
    }

    def deserializeJCell(jcell: JsValue, grid: MutMap[Posn, Tile], rowIdx: Int): Unit = {
        jcell match {
            case JsArray(elements) => {
                elements.head match {
                    case JsNumber(colIdx) => {
                        val tile: Tile = deserializeJTile(elements.tail.head)
                        grid += (Posn(colIdx.toInt, -1 * rowIdx) -> tile)
                    }
                    case _ => throw new IllegalArgumentException("First element of JCell was not an Integer.")
                }
            }
            case _ => throw new IllegalArgumentException("JCell was not a JSON array.")
        }
    }

    def deserializeJTile(jtile: JsValue): Tile = {
        jtile match {
            case JsObject(fields) => {
                fields.get("shape") match {
                    case Some(JsString(shapeStr)) => {
                        val shape: Shape = atomic.Shape.withName(shapeStr)
                        fields.get("color") match {
                            case Some(JsString(colorStr)) => {
                                val color: Color = atomic.Color.withName(colorStr)
                                return Tile(color, shape)
                            }
                            case _ => throw new IllegalArgumentException("JTile did not contain 'color' field.")
                        }
                    }
                    case _ => throw new IllegalArgumentException("JTile did not contain 'shape' field.")
                }
            }
            case _ => throw new IllegalArgumentException("JTile was not a JSON Object.")
        }
    }

    def deserializeJPub(jpub: JsValue): TurnInfo = {
        jpub match {
            case JsObject(fields) => {
                fields.get("tile*") match {
                    case Some(JsNumber(tilesRemaining)) => {
                        fields.get("map") match {
                            case Some(mapJs) => {
                                val map = deserializeJMap(mapJs).getMap
                                fields.get("players") match {
                                    case Some(JsArray(players)) => {
                                        val privatePlayer = deserializeJPlayer(players.head)
                                        val publicPlayers = players.tail.map(deserializePublicPlayer(_))
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

    def deserializeJPubToGameState(jpub: JsValue): GameState = deserializeJPub(jpub).toPrivateGameState

    def deserializePublicPlayer(pubplayer: JsValue): PublicPlayerInfo = {
        pubplayer match {
            case JsNumber(score) => PublicPlayerInfo(score.toInt)
            case _ => throw new IllegalArgumentException("Tail element of JPub 'players' was not a JSON number.")
        }
    }

    def deserializeJPlayer(jplayer: JsValue): PrivatePlayerInfo = {
        jplayer match {
            case JsObject(fields) => {
                fields.get("tile*") match {
                    case Some(JsArray(tiles)) => {
                        val deserializedTiles: List[Tile] = tiles.map(deserializeJTile(_)).toList
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

    def deserializeJPlacements(jplacements: JsValue): List[Placement] = {
        jplacements match {
            case JsArray(elements) => {
                elements.map(deserialize1Placement(_)).toList
            }
            case _ => throw new IllegalArgumentException("JPlacements was not a JSON Array.")
        }
    }

    def deserialize1Placement(placement: JsValue): Placement = {
        placement match {
            case JsObject(fields) => {
                fields.get("coordinate") match {
                    case Some(jcoordinate) => {
                        fields.get("1tile") match {
                            case Some(jtile) => Placement(deserializeJCoordinate(jcoordinate), deserializeJTile(jtile))
                            case None => throw new IllegalArgumentException("1Placement did not contain field '1tile'.")
                        }
                    }
                    case None => throw new IllegalArgumentException("1Placement did not contain field 'coordinate'.")
                }
            }
            case _ => throw new IllegalArgumentException("1Placement was not a JSON Object.")
        }
    }

    def serialize1Placement(placement: Placement): JsObject = {
        JsObject(Map(
            "coordinate" -> serializePosnToJCoordinate(placement.pos),
            "1tile" -> serializeJTile(placement.tile)
        ))
    }

    def deserializeJCoordinate(jcoordinate: JsValue): Posn = {
        jcoordinate match {
            case JsObject(fields) => {
                fields.get("row") match {
                    case Some(JsNumber(row)) => {
                        fields.get("column") match {
                            case Some(JsNumber(col)) => {
                                Posn(col.toInt, -1 * row.toInt)
                            }
                            case _ => throw new IllegalArgumentException("Badly formed JCoordinate.")
                        }
                    }
                    case _ => throw new IllegalArgumentException("Badly formed JCoordinate.")
                }

            }
            case _ => throw new IllegalArgumentException("JCoordinate was not a JSON object.")
        }
    }

    def deserializeJStrategy(jstrategy: JsValue): Strategy = jstrategy match {
        case JsString(value) => value match {
            case "dag" => DAGStrategy
            case "ldasg" => LDASGStrategy
            case _ => throw new IllegalArgumentException("Provided strategy was not a valid strategy.")
        }
        case _ => throw new IllegalArgumentException("JStrategy was not a JSON string.")
    }

    def getNextJson: Option[JsValue] = {
        var line = scala.io.StdIn.readLine()
        var currArr = ""

        while (line != null && line.nonEmpty) {
            currArr += line
            try {
                // check if current array is complete JSON
                // this flow control is dependent on the following truth: valid JSON + one extra line is not valid JSON,
                // i.e. the JSON is complete as soon as it is valid
                val json = currArr.parseJson
                return Some(json)
            } catch {
                case e: JsonParser.ParsingException =>
                // this case is ok, we continue trying to build a complete array
                case e: RuntimeException => throw e
            } 
        }
        return None
    }

    def sendJson(json: JsValue): Unit = {
        println(json.prettyPrint)
    }
}