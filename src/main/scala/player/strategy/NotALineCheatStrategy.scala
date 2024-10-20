package player.strategy 

import common.TurnInfo
import common.Placement
import atomic.Direction
import atomic.Posn
import player.Action
import player.PlaceAction

/*
The NotALineCheatStrategy attempts to cheat by placing tiles that are not in the same row or column.
If it cannot, it reverts to its base strategy.
*/
class NotALineCheatStrategy(private val base: Strategy) extends Strategy {

    override def produceAction(info: TurnInfo): Action = findANonLinearPlacement(info)

    private def findANonLinearPlacement(info: TurnInfo): Action = {
        base.produceAction(info) match {
            case PlaceAction(placements) => {
                val tempInfo = StrategyIterator.updateTurnInfo(info, placements)
                recur(info, tempInfo, PlaceAction(placements))
            }
            case a => a
        }
    }

    /*
    Generates the maximal placements of a PlaceAction according to the strategy and the provided state.
    Goes one step further and includes the first illegal placement in the sequence 
    so as to generate a non-linear placement.
    */
    private def recur(originalInfo: TurnInfo, accInfo: TurnInfo, accAction: PlaceAction): Action = {
        base.produceAction(accInfo) match {
            case PlaceAction(placements) => {
                val tempState = originalInfo.toPrivateGameState
                if (tempState.isValidAction(PlaceAction(accAction.placements ++ placements))) {
                    val tempInfo = StrategyIterator.updateTurnInfo(accInfo, placements)
                    recur(originalInfo, tempInfo, PlaceAction(accAction.placements ++ placements))
                } else return PlaceAction(accAction.placements ++ placements)
                
            }
            case _ => accAction
        }
    }
}