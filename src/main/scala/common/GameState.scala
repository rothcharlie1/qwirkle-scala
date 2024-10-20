package common

import spray.json._
import java.awt.image.BufferedImage
import java.awt.Image
import java.awt.Graphics2D
import java.awt.{Color => AwtColor}
import java.awt.Font
import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import javax.swing.{ImageIcon, JFrame, JLabel, WindowConstants}
import scala.util.Random

import atomic.Color.Color 
import atomic.Shape.Shape
import atomic.Color
import atomic.Shape
import atomic.Posn
import util.Constants
import player.ExchangeAction
import player.PassAction
import player.PlaceAction
import player.Action

/*
The Q Game state. Tracks the state players in the game, the game board, and the turns of the players.
*/
class GameState(private var players: List[PlayerState], originTile: Tile, private var refereeTiles: List[Tile], private var fixed: Boolean = false) {

    // initial player list is sorted in descending age order
    if (!fixed) players  = players.sorted.reverse
    private var activePlayer: PlayerState = players.head
    private var gameMap = new MapMap(Some(originTile))
    private var scoringConfig = ScoringConfig(8, 4)
    

    // construct a state from a preexisting map
    def this(map: MapMap, players: List[PlayerState]) = {
        this(players, Tile(Color.RED, Shape.SQUARE), List[Tile](), true) // red square argument is ignored, fix tiles and players
        this.gameMap = map
        
        for (color <- Color.values) {
            for (shape <- Shape.values) {
                for (_ <- 1 to Constants.NUM_EACH_TILE) {
                    refereeTiles = Tile(color, shape) :: refereeTiles
                }
            }
        }
        refereeTiles = refereeTiles diff (originTile :: players.flatMap(_.tiles)) 
    }

    def this(map: MapMap, players: List[PlayerState], tiles: List[Tile]) = {
        this(map, players)
        refereeTiles = tiles
    }

    /*
    Set the scoring configuration to be used for this game.
    */
    def setScoringConfig(cfg: ScoringConfig): Unit = {
        scoringConfig = cfg
    }

    /*
    Provides the info the active player needs to play its turn.
    */
    def getTurnInfo: TurnInfo = {
        TurnInfo(
            this.activePlayer.getPrivatePlayerInfo, 
            this.players.tail.map(player => player.getPublicPlayerInfo), 
            refereeTiles.size, 
            this.gameMap.copy
        )
    }

    /*
    Delegates the provided Action to the correct method for execution.
    */
    def executeAction(action: Action): List[Tile] = action match {
        case ExchangeAction => takeExchangeTurn
        case PassAction => takePassTurn
        case PlaceAction(placements) => takePlacementTurn(placements)
    }

    /*
    Passes the active player's turn, and returns no new Tiles.
    */
    private def takePassTurn: List[Tile] = {
        this.rotateActivePlayer(0)
        List[Tile]()
    }

    /*
    Exchanges all of the active player's tiles with a random subset of the referee tiles
    @return The List of the active player's previous Tiles
    @throws RuntimeException if there are not enough referee tiles
    */
    private def takeExchangeTurn: List[Tile] = {
        if (refereeTiles.size < activePlayer.tiles.size) throw new IllegalArgumentException("Referee does not have enough tiles.")
        val swapTiles = drawTiles(activePlayer.tiles.size)
        val returnTiles = this.activePlayer.swapTiles(swapTiles)
        this.rotateActivePlayer(0)
        refereeTiles = refereeTiles ++ returnTiles
        swapTiles
    } 

    /*
    Removes the provided number of tiles from the referee deck and returns them.
    */
    private def drawTiles(size: Int): List[Tile] = {
        fixed match {
            case false => {
                val tiles = Random.shuffle(refereeTiles).take(activePlayer.tiles.size)
                refereeTiles = refereeTiles diff tiles
                tiles
            }
            case true => {
                val tiles = refereeTiles.take(activePlayer.tiles.size)
                refereeTiles = refereeTiles diff tiles
                tiles
            }
        }
    }

    /*
    Places a tile as either the first or a subsequent placement in a turn.

    @param tile The Tile to place.
    @param pos The Posn to insert at.
    */
    private def takePlacementTurn(placements: List[Placement]): List[Tile] = {
        if (!isValidPlacement(placements)) throw new IllegalArgumentException("Placement is not valid.")
        for (placement <- placements) {
            gameMap.insertTile(placement.tile, placement.pos)
            this.activePlayer.removeTile(placement.tile)
        }
        val newTiles = drawTiles(placements.size)
        activePlayer.giveTiles(newTiles)
        rotateActivePlayer(scorePlacement(placements))
        newTiles
    }

    /*
    Moves the active player to the back of the queue and resets all turn-specific variables.
    */
    private def rotateActivePlayer(score: Int): Unit = {
        var newPlayers = this.players match {
            case head :: tail => tail :+ head
            case Nil => Nil
        }
        this.players = newPlayers
        this.activePlayer.addTurnScore(score)
        this.activePlayer = this.players.head
    }

    /*
    Removes the current player from the game and rotates the active player
    */
    def removeActivePlayer: Unit = {
        this.players = this.players.tail
        if (this.players.size > 1) this.activePlayer = this.players.head
    }

    def getWinners: List[String] = {
        if (players.size == 0) return List[String]()
        val sortedByScore = players.sortBy(_.getScore).reverse
        var winners = List[PlayerState](sortedByScore.head)
        for (ps <- sortedByScore.tail) {
            if (ps.getScore != winners.head.getScore) return winners.map(_.id)
            else winners = ps :: winners
        }
        return winners.map(_.id)
    }

    /*
    Scores the provided placement according to the rules of Q. 
    Assumes the provided placement has already been placed on the board.
    */
    def scorePlacement(placements: List[Placement]): Int = {
        var seqs = placements.map(_.pos)
                             .map(this.gameMap.findContiguousSequencesFromPosition(_))
                             .flatten
                             .distinct
        var score: Int = placements.size
        for (seq <- seqs) {
            score += seq.size
            val tiles = seq.map(this.gameMap.getTileAtPosition(_).get).toList
            val shapes = tiles.map(_.shape)
            val colors = tiles.map(_.color)
            if (Color.hasOneOfEach(colors) || Shape.hasOneOfEach(shapes)) score += scoringConfig.qBonus
        }
        if (!this.activePlayer.hasTiles) score += scoringConfig.finishBonus
        score
    }

    /*
    Decides if the provided Action is valid under this Game State.
    */
    def isValidAction(action: Action): Boolean = {
        action match {
            case PlaceAction(placements) => isValidPlacement(placements)
            case PassAction => true
            case ExchangeAction => {
                refereeTiles.size >= activePlayer.tiles.size
            }
        }
    }

    /*
    Decides if the provided series of Placements is legal starting at the current game state position.
    The current active player must hold the tiles in question.
    */
    def isValidPlacement(placements: List[Placement]): Boolean = {
        if (!activePlayerHoldsTiles(placements.map(_.tile))) false
        else if (!placementsInSameRowOrCol(placements)) false
        else {
            val tempMap = gameMap.copy
            for (placement <- placements) {
                if (!tempMap.getValidTileInsertions(placement.tile).contains(placement.pos)) return false
                tempMap.insertTile(placement.tile, placement.pos)
            }
            true
        } 
    }

    /*
    Get all positions where the given tile can legally be placed.
    */
    def getValidTileInsertions(tile: Tile): List[Posn] = this.gameMap.getValidTileInsertions(tile)

    /*
    Check that the currently active player holds all tiles in a list.
    BUG: if multiple of the same tile in a placement, does not check if player holds multiple.
    */
    def activePlayerHoldsTiles(tiles: List[Tile]): Boolean = {
        tiles.forall(this.activePlayer.holdsTile(_))
    }

    /*
    Checks if a series of Placements are either in the same row or same column.
    */ 
    def placementsInSameRowOrCol(placements: List[Placement]): Boolean = {
        var positions = placements.map(_.pos)
        positions match {
            case head :: tail => head.allInSameCol(tail) || head.allInSameRow(tail)
            case Nil => true
        }
    }

    /*
    Returns a copy of the current board and its tiles, for public viewing.
    */
    def getMap: Map[Posn, Tile] = gameMap.getMap

    def copy: GameState = new GameState(gameMap.copy, players.map(_.copy), refereeTiles)

    def toJState: JsValue = {
        val map = gameMap.toJMap
        val tiles = JsArray(refereeTiles.map(_.toJTile))
        val jplayers = JsArray(players.map(_.toJPlayer))
        JsObject(Map("map" -> map, "tile*" -> tiles, "players" -> jplayers))
    }

    /*
    Constructs an Image instance showing the current public game state.
    */
    def render: BufferedImage = {
        val mapImage = gameMap.render
        val scoresImage = renderPlayerScores
        val remainingTilesImage = renderRemainingTiles

        val maxY = mapImage.getHeight(null).max(scoresImage.getHeight(null) + remainingTilesImage.getHeight(null))
        val maxX = scoresImage.getWidth(null) + mapImage.getWidth(null)

        val combinedImage = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = combinedImage.createGraphics()
        g2d.setColor(AwtColor.WHITE)
        g2d.fillRect(0, 0, combinedImage.getWidth(null), combinedImage.getHeight(null))

        g2d.drawImage(remainingTilesImage, 0, 0, null)
        g2d.drawImage(scoresImage, 0, remainingTilesImage.getHeight(null), null)
        g2d.drawImage(mapImage, remainingTilesImage.getWidth(null), 0, null)

        g2d.dispose()
        combinedImage
    }

    /*
    Renders the component of the public game state image that shows the scores of each player in play order.
    */
    def renderPlayerScores: Image = {
        val image = new BufferedImage(200, (players.size + 1) * 100, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()
        g2d.setColor(AwtColor.WHITE)
        g2d.fillRect(0, 0, image.getWidth(null), image.getHeight(null))
        g2d.setColor(AwtColor.BLACK)
        g2d.setFont(new Font("Arial", Font.BOLD, 15))

        // draw label
        g2d.drawString("Player Scores:", 30, 50)

        val sortedPlayers = players.sortBy(_.id)

        for (i <- 1 to sortedPlayers.size) {
            val score = sortedPlayers(i - 1).getScore
            val name = sortedPlayers(i-1).id
            g2d.drawString(s"Player $name: $score", 30, (i * 100) + 50)
        }
        g2d.dispose()
        image
    }

    /*
    Renders the piece of the game state image that notes the number of remaining tiles.
    */
    def renderRemainingTiles: Image = {
        var remainingTiles = Constants.TOTAL_TILES - gameMap.numTilesPlaced
        val image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()

        g2d.setColor(AwtColor.WHITE)
        g2d.fillRect(0, 0, image.getWidth(null), image.getHeight(null))
        g2d.setColor(AwtColor.BLACK)
        g2d.setFont(new Font("Arial", Font.BOLD, 15))

        g2d.drawString(s"Remaining tiles: $remainingTiles", 30, 50)
        g2d.dispose()
        image
    }
}