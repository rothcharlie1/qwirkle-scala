package player

import common.MapMap
import common.TurnInfo
import common.Tile
import strategy.{Strategy, StrategyIterator}

/*
A Player that uses a provided Strategy to choose moves. 
A StrategyPlayer is entirely stateless and relies only on incoming TurnInfo to make decisions.
*/
class StrategyPlayer(private val id: String, private val strategy: Strategy) extends Player {

    /*
    Identifies this Player with a name String.
    */
    def name: String = id

    /*
    Sets up this Player with an initial game map and a bag of tiles.
    */
    def setup(info: TurnInfo, st: List[Tile]): Unit = {}

    /*
    Returns this Player's requested turn Action given its private TurnInfo.
    */
    def takeTurn(info: TurnInfo): Action = new StrategyIterator(strategy).iterate(info)

    /*
    Provides the Player with new Tiles to replace those it lost.
    */
    def newTiles(tiles: List[Tile]): Unit = {}

    /*
    Informs this Player of the end of the game.

    @param w True if this Player won, false otherwise.
    */
    def win(w: Boolean): Unit = {}
}