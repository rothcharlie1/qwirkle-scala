package player

import org.scalatest.flatspec.AnyFlatSpec
import common.Tile
import atomic.Color._ 
import atomic.Shape._ 
import common.PlayerState
import common.GameState
import common.MapMap
import atomic.Posn
import common.Placement
import strategy.{DAGStrategy, LDASGStrategy, StrategyIterator}


class StrategySpec extends AnyFlatSpec {

    var neededTiles = List(
        Tile(RED, STAR), 
        Tile(RED, EIGHTSTAR),
    )
    var player = new PlayerState(neededTiles, 20, "player", 0)
    

    var map = new MapMap(Some(Tile(GREEN, CIRCLE)))
    map.insertTile(Tile(RED, CIRCLE), Posn(0, 1))
    map.insertTile(Tile(RED, STAR), Posn(1, 1))
    map.insertTile(Tile(RED, DIAMOND), Posn(2, 1))
    map.insertTile(Tile(GREEN, DIAMOND), Posn(2, 0))
    map.insertTile(Tile(GREEN, STAR), Posn(1, 0))

    var gameState: GameState = new GameState(map, List(player))
    var turninfo = gameState.getTurnInfo

    behavior of "The DAG Strategy"

    it should "try to place a Red Star at (0,2)" in {
        DAGStrategy.produceAction(turninfo) match {
            case PlaceAction(placements) => {
                assert(placements(0).tile.equals(Tile(RED, STAR)) && placements(0).pos.equals(Posn(0, 2)))
            }
            case _ => assert(false)
        }
    }

    behavior of "The LDASG Strategy"

    it should "place a Red Star in a constrained horseshoe gap" in {
        var map2 = new MapMap(Some(Tile(RED, CIRCLE)))
        map2.insertTile(Tile(GREEN, CIRCLE), Posn(0, 1))
        map2.insertTile(Tile(GREEN, STAR), Posn(1, 1))
        map2.insertTile(Tile(GREEN, DIAMOND), Posn(2, 1))
        map2.insertTile(Tile(RED, DIAMOND), Posn(2, 0))
        var gameState2: GameState = new GameState(map2, List(player))
        var turninfo2 = gameState2.getTurnInfo
        LDASGStrategy.produceAction(turninfo2) match {
            case PlaceAction(placements) => {
                println(placements(0).tile.toString())
                println(placements(0).pos.toString())
                assert(placements(0).tile.equals(Tile(RED, STAR)) && placements(0).pos.equals(Posn(1, 0)))
            }
        }
    }

    behavior of "a DAG Iterator"

    it should "place a red star and then a red eightstar" in {
        var map3 = new MapMap(Some(Tile(RED, CIRCLE)))
        map3.insertTile(Tile(GREEN, CIRCLE), Posn(0, 1))
        map3.insertTile(Tile(GREEN, STAR), Posn(1, 1))
        map3.insertTile(Tile(GREEN, DIAMOND), Posn(2, 1))
        map3.insertTile(Tile(RED, DIAMOND), Posn(2, 0))
        var gameState3: GameState = new GameState(map3, List(player))
        var turninfo3 = gameState3.getTurnInfo
        var siter = new StrategyIterator(LDASGStrategy).iterate(turninfo3)
        
    }
    // test is currently failing
    // strategy iterator appears to be running infinitely and I'm not sure why



}