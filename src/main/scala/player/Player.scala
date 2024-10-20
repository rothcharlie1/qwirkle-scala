package player

import common.MapMap
import common.Tile
import common.TurnInfo

/*
The required public functionality of a Q Player, remote or local.
*/
trait Player {

    /*
    Identifies this Player with a name String.
    */
    def name: String

    /*
    Sets up this Player with an initial game map and a bag of tiles.
    */
    def setup(state: TurnInfo, tiles: List[Tile]): Unit

    /*
    Returns this Player's requested turn Action given its private TurnInfo.
    */
    def takeTurn(info: TurnInfo): Action

    /*
    Provides the Player with new Tiles to replace those it lost. 
    */
    def newTiles(tiles: List[Tile]): Unit

    /*
    Informs this Player of the end of the game.

    @param w True if this Player won, false otherwise.
    */
    def win(w: Boolean): Unit

}