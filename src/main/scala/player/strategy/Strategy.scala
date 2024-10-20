package player.strategy

import common.TurnInfo
import common.Tile
import player.Action

/*
A strategy employed by a player to play Q. 
Provides a function to produce actions from provided turn info.
*/
trait Strategy {

    /*
    Given a piece of turn information, generate the next action the strategy takes.
    */
    def produceAction(info: TurnInfo): Action

    /*
    Finds the smallest tile in 'tiles' that can be inserted according to the TurnInfo 'info'.
    */
    protected def firstValidTile(tiles: List[Tile], info: TurnInfo): Option[Tile] = {
        tiles.sorted match {
            //TODO: remove dependency on fake game state
            case tile :: rest => info.toPrivateGameState.getValidTileInsertions(tile) match {
                case Nil => firstValidTile(rest, info)
                case posn :: posns => Some(tile)
            }
            case Nil => None
        }
    }

}