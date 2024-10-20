package player.strategy

import common.TurnInfo
import common.Placement
import atomic.Direction
import atomic.Posn
import player.Action
import player.PlaceAction

/*
A NoFitCheatStrategy attempts to cheat by placing a tile that does not match its neighbors.
If it cannot, it reverts to its base Strategy's action.
*/
class NoFitCheatStrategy(base: Strategy) extends Strategy {


    override def produceAction(info: TurnInfo): Action = getNonFittingPlacement(info)

    private def getNonFittingPlacement(info: TurnInfo): Action = {
        val tile = info.player.tiles.head
        var possiblePosn = Posn(0,0)
        while (info.map.hasTileNeighbor(possiblePosn) && info.map.fits(tile, possiblePosn)) {
            possiblePosn = possiblePosn.inDirection(Direction.UP)
        }
        if (possiblePosn == Posn(0,0)) base.produceAction(info) 
        else PlaceAction(Placement(possiblePosn, tile) :: Nil)
    }
}