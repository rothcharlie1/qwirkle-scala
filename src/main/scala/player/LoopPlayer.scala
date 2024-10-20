package player

import common.MapMap
import common.Tile
import common.TurnInfo
import strategy.Strategy

class LoopPlayer(id: String, strategy: Strategy, method: String, count: Int) extends StrategyPlayer(id, strategy) {
    private var callCount = 0
    /*
    Sets up this Player with an initial game map and a bag of tiles.
    */
    override def setup(info: TurnInfo, st: List[Tile]): Unit = {
        if (method == "setup") {
            callCount += 1
            if (callCount == count) {
                while (true) {}
            }
        }
        super.setup(info, st)
    }

    /*
    Returns this Player's requested turn Action given its private TurnInfo.
    */
    override def takeTurn(info: TurnInfo): Action = {
        if (method == "take-turn") {
            callCount += 1
            if (callCount == count) {
                while (true) {}
            }
        }
        super.takeTurn(info)
    }

    /*
    Provides the Player with new Tiles to replace those it lost.
    */
    override def newTiles(tiles: List[Tile]): Unit = {
        if (method == "new-tiles") {
            callCount += 1
            if (callCount == count) {
                while (true) {}
            }
        }
        super.newTiles(tiles)
    }

    /*
    Informs this Player of the end of the game.

    @param w True if this Player won, false otherwise.
    */
    override def win(w: Boolean): Unit = {
        if (method == "win") {
            callCount += 1
            if (callCount == count) {
                while (true) {}
            }
        }
        super.win(w)
    }
}