package player.strategy

import common.Placement
import common.Tile
import common.TurnInfo
import common.GameState
import atomic.Posn
import player.{Action, PlaceAction, ExchangeAction, PassAction}

/*
A Q Strategy that selects the 'smallest' tile and the most constrained (most neighboring tiles) position for placement.
*/
object LDASGStrategy extends Strategy {

    /*
    Produces an action according to the defined LDASG strategy, given the private turn info.
    */
    def produceAction(info: TurnInfo): Action = {
        val privateGameState = info.toPrivateGameState
        val tiles = info.player.tiles.sorted
        findTile(tiles, privateGameState, info) match {
            case Some(action) => action
            case None => exchangeIfPossible(info)
        }
    }

    /*
    Attempts to create a PlaceAction with the provided tiles and a private game state.

    @param tiles A sorted list of tiles to check for placements
    @param gameState a GameState object the strategy uses to check validity
    @param info The current TurnInfo

    @returns A Some(PlaceAction) if there is a valid tile placement, else None
    */
    private def findTile(tiles: List[Tile], gameState: GameState, info: TurnInfo): Option[PlaceAction] = {
        super.firstValidTile(tiles, info) match {
            case None => None
            case Some(tile) => {
                gameState.getValidTileInsertions(tile).sorted match {
                    case posn :: posntail => {
                        val bestPosn = mostConstrainedPosn(posn, posntail, info)
                        Some(PlaceAction(List(Placement(bestPosn, tile))))
                    }
                    case Nil => throw new IllegalArgumentException
                }
            } 
        }
    }

    /*
    Given a list of positions, find the one with the most neighbor tiles (a proxy for the highest fit difficulty for a tile).

    @param bestPos The current most constrained position
    @param positions The remaining positions over which to recur
    @param info The current TurnInfo

    @returns The Posn with the most neighbor tiles, or the lowest in row-column order in the event of a tie.
    */
    private def mostConstrainedPosn(bestPos: Posn, positions: List[Posn], info: TurnInfo): Posn = positions match {
        case Nil => bestPos
        case posn :: tail => countNeighbors(posn, info).compare(countNeighbors(bestPos, info)) match {
            case x if x > 0 => mostConstrainedPosn(posn, tail, info)
            case _ => mostConstrainedPosn(bestPos, tail, info)
        }
    }

    /*
    Counts the number of neighboring tiles to a position.
    */
    private def countNeighbors(pos: Posn, info: TurnInfo): Int = {
        pos.neighbors.map(info.map.getMap.get(_))
                     .filter(p => p != None)
                     .size
    }

    /*
    Provides an ExchangeAction if possible according to the game rules, else a PassAction
    */
    private def exchangeIfPossible(info: TurnInfo): Action = {
        info.player.tiles.size <= info.tilesRemaining match {
            case true => ExchangeAction
            case false => PassAction
        }
    }
}