package player.strategy

import common.TurnInfo
import common.Placement
import common.Tile
import atomic.Direction
import atomic.Posn
import atomic.PrivatePlayerInfo
import atomic.Color
import atomic.Shape
import player.Action
import player.PlaceAction


class TileNotOwnedCheatStrategy(base: Strategy) extends Strategy {

    override def produceAction(info: TurnInfo): Action = getNotOwnedTileAction(info)

    private def getNotOwnedTileAction(info: TurnInfo): Action = { 
        val newPrivatePlayerInfo = PrivatePlayerInfo(
            generateSingleCopyOfAllTiles.filter(tile => !info.player.tiles.contains(tile)).toList, 
            info.player.score,
            info.player.id
        )
        val newInfo: TurnInfo = new TurnInfo(newPrivatePlayerInfo, info.opponents, info.tilesRemaining, info.map)
        base.produceAction(newInfo)
    }

    private def generateSingleCopyOfAllTiles: List[Tile] = {
        var allTiles: List[Tile] = Nil
        for (shape <- Shape.values) {
            for (color <- Color.values) {
                allTiles = Tile(color, shape) :: allTiles
            }
        }
        allTiles
    }
}