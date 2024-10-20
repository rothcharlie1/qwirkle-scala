package player.strategy

import common.GameState
import common.TurnInfo
import atomic.PrivatePlayerInfo
import common.Placement
import common.MapMap
import player.{Action, PlaceAction}

/*
Provides functionality for a given Strategy to be applied repeatedly until no more actions are possible.
*/
class StrategyIterator(strategy: Strategy) {

    /*
    Executes a single turn of the strategy on TurnInfo 'info' to its maximum depth
    TODO: ensure placements are in same row or col
    */
    def iterate(info: TurnInfo): Action = {

        strategy.produceAction(info) match {
            case PlaceAction(placements) => {
                val tempInfo = StrategyIterator.updateTurnInfo(info, placements)
                recur(info, tempInfo, PlaceAction(placements))
            }
            case a => a
        }
    }

    /*
    Generates the maximal placements of a PlaceAction according to the strategy and the provided state.
    */
    private def recur(originalInfo: TurnInfo, accInfo: TurnInfo, accAction: PlaceAction): Action = {
        strategy.produceAction(accInfo) match {
            case PlaceAction(placements) => {
                val tempState = originalInfo.toPrivateGameState
                if (tempState.isValidAction(PlaceAction(accAction.placements ++ placements))) {
                    val tempInfo = StrategyIterator.updateTurnInfo(accInfo, placements)
                    recur(originalInfo, tempInfo, PlaceAction(accAction.placements ++ placements))
                } else return accAction
                
            }
            case _ => accAction
        }
    }
}

object StrategyIterator {
    /*
    Updates a TurnInfo by applying a series of Placements
    */
    def updateTurnInfo(info: TurnInfo, placements: List[Placement]): TurnInfo = {
        var tempPrivatePlayer: PrivatePlayerInfo = info.player
        var tempMap = info.map.getMap
        for (plmt <- placements) {
            tempPrivatePlayer = PrivatePlayerInfo(tempPrivatePlayer.tiles diff List(plmt.tile), tempPrivatePlayer.score, tempPrivatePlayer.id)
            tempMap = tempMap + (plmt.pos -> plmt.tile)
        }
        TurnInfo(tempPrivatePlayer, info.opponents, info.tilesRemaining, new MapMap(tempMap))
    }
}