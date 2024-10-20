package common

import org.scalatest.flatspec.AnyFlatSpec
import common.Tile
import common.Placement
import atomic.Posn
import atomic.Color._ 
import atomic.Shape._
import player.PlaceAction

class GameStateSpec extends AnyFlatSpec {

    var neededTiles = List( 
        Tile(RED, CIRCLE), 
        Tile(RED, CLOVER), 
        Tile(RED, DIAMOND), 
        Tile(RED, STAR), 
        Tile(GREEN, EIGHTSTAR),
        Tile(RED, EIGHTSTAR), 
        Tile(ORANGE, CIRCLE),
        Tile(RED, EIGHTSTAR),
        Tile(RED, EIGHTSTAR)
    )
    var player = new PlayerState(neededTiles, 20, "player", 0)
    var gameState: GameState = new GameState(List(player), Tile(RED, SQUARE), List())
    var placements: List[Placement] = List(
        Placement(Posn(0, 1), Tile(RED, CIRCLE)),
        Placement(Posn(0, 2), Tile(RED, CLOVER)),
        Placement(Posn(0,3), Tile(RED, DIAMOND)),
        Placement(Posn(0,4), Tile(RED, STAR))
    )
    behavior of "A game state with one RED Square tile"

    it should "accept a sequence of tiles that is almost a Q" in {
        
        assert(gameState.isValidPlacement(placements) == true)
        gameState.executeAction(PlaceAction(placements))
    }

    it should "score the placement a 9" in {
        assert(gameState.scorePlacement(placements) == 9)
    }

    it should "accept a RED 8Star and a GREEN 8Star" in {
        assert(gameState.isValidPlacement(
                List(
                    Placement(Posn(0,5), Tile(RED, EIGHTSTAR)), 
                    Placement(Posn(1,5), Tile(GREEN, EIGHTSTAR))
                )
                )
                == true)
        gameState.executeAction(PlaceAction(List(
                    Placement(Posn(0,5), Tile(RED, EIGHTSTAR)), 
                    Placement(Posn(1,5), Tile(GREEN, EIGHTSTAR))
                )))
    }

    // failing
    // I think the score should be 17 but my code says 16
    it should "score a Q for the previous placement" in {
        assert(gameState.scorePlacement(List(
                    Placement(Posn(0,5), Tile(RED, EIGHTSTAR)), 
                    Placement(Posn(1,5), Tile(GREEN, EIGHTSTAR))
                )) == 16)
    }

    it should "not accept a placement of a tile the player does not have" in {
        assert(gameState.isValidPlacement(List(Placement(Posn(0,6), Tile(PURPLE, EIGHTSTAR))))
                == false)
    }

    it should "not accept placement of tiles that are not in a row or column" in {
        assert(gameState.isValidPlacement(
                List(
                    Placement(Posn(-1,1), Tile(RED, EIGHTSTAR)), 
                    Placement(Posn(1,2), Tile(RED, EIGHTSTAR))
                )
                )
                == false)
    }

}