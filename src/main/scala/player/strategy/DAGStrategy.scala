package player.strategy

import common.TurnInfo
import common.Tile
import common.GameState
import common.Placement
import player.{Action, PlaceAction, ExchangeAction, PassAction}

/*
A Q Strategy that chooses the first (according to both tile and posn ordering rules) placement possible.
If no placement is possible, it tries to exchange tiles if possible, else it passes.
*/
object DAGStrategy extends Strategy {

    /*
    Produces an action according to the defined DAG strategy, given the private turn info.
    */
    def produceAction(info: TurnInfo): Action = {
        val privateGameState = info.toPrivateGameState
        val tiles = info.player.tiles.sorted
        findTile(tiles, info, privateGameState) match {
            case Some(action) => action
            case None => exchangeIfPossible(tiles, info)
        }
    }

    /*
    Attempts to create a PlaceAction with the provided tiles and a private game state.

    @param tiles A sorted list of tiles to check for placements
    @param gameState a GameState object the strategy uses to check validity

    @returns A Some(PlaceAction) if there is a valid tile placement, else None
    */
    private def findTile(tiles: List[Tile], info: TurnInfo, gameState: GameState): Option[PlaceAction] = {
        super.firstValidTile(tiles, info) match {
            case None => None
            case Some(tile) => {
                gameState.getValidTileInsertions(tile).sorted match {
                    case posn :: posntail => Some(PlaceAction(List(Placement(posn, tile))))
                    case Nil => throw new IllegalArgumentException
                }
            } 
        }
    }

    /*
    Provides an ExchangeAction if possible according to the game rules, else a PassAction
    */
    private def exchangeIfPossible(tiles: List[Tile], info: TurnInfo): Action = {
        tiles.size <= info.tilesRemaining match {
            case true => ExchangeAction
            case false => PassAction
        }
    }

}