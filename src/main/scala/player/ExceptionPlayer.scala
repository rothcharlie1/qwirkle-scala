package player

import common.MapMap
import common.Tile
import common.TurnInfo
import strategy.Strategy

class ExceptionPlayer(id: String, strategy: Strategy, method: String) extends StrategyPlayer(id, strategy) {
    
    /*
    Sets up this Player with an initial game map and a bag of tiles.
    */
    override def setup(info: TurnInfo, st: List[Tile]): Unit = {
        if (method == "setup") throw new Exception("Cannot retrieve setup from this player")
        super.setup(info, st)
    }

    /*
    Returns this Player's requested turn Action given its private TurnInfo.
    */
    override def takeTurn(info: TurnInfo): Action = {
        if (method == "take-turn") throw new Exception("Cannot retrieve take-turn from this player")
        super.takeTurn(info)
    }

    /*
    Provides the Player with new Tiles to replace those it lost.
    */
    override def newTiles(tiles: List[Tile]): Unit = {
        if (method == "new-tiles") throw new Exception("Cannot retrieve new-tiles from this player")
        super.newTiles(tiles)
    }

    /*
    Informs this Player of the end of the game.

    @param w True if this Player won, false otherwise.
    */
    override def win(w: Boolean): Unit = {
        if (method == "win") throw new Exception("Cannot retrieve win from this player")
        super.win(w)
    }
}