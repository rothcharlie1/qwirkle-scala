package player.strategy

import common.TurnInfo
import atomic.Posn
import common.Placement 
import player.{Action, PlaceAction}
import atomic.Direction

/*
A NonAdjacentCheatStrategy is a Strategy that attempts to cheat by placing a tile such that it has no neighbors on the map.
If it cannot cheat (i.e., the Player has no tiles), it reverts to its base strategy. 
*/
class NonAdjacentCheatStrategy(base: Strategy) extends Strategy {

    override def produceAction(info: TurnInfo): Action = getNonAdjacentPlacement(info)

    private def getNonAdjacentPlacement(info: TurnInfo): Action = {
        if (info.player.tiles.size == 0) return base.produceAction(info)

        val tile = info.player.tiles.head
        var possiblePosn = Posn(0,0)
        while (info.map.hasTileNeighbor(possiblePosn)) {
            possiblePosn = possiblePosn.inDirection(Direction.UP)
        }
        PlaceAction(Placement(possiblePosn, tile) :: Nil)
    }
}