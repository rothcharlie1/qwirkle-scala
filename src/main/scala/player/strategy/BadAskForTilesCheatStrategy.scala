package player.strategy 

import common.TurnInfo
import common.Placement
import atomic.Direction
import atomic.Posn
import player.Action
import player.ExchangeAction

class BadAskForTilesCheatStrategy(base: Strategy) extends Strategy {

    override def produceAction(info: TurnInfo): Action = {
        if (info.player.tiles.size > info.tilesRemaining) ExchangeAction
        else base.produceAction(info)
    }
}