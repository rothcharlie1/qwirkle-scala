package referee

import scala.util.Random

import player.Player
import common.GameState
import common.Tile
import atomic.Color
import atomic.Shape
import util.Constants
import common.PlayerState
import common.TurnInfo
import atomic.Posn
import common.MapMap
import player.PassAction
import player.Action
import player.ExchangeAction
import player.PlaceAction
import TurnResult.TurnResult

/*
A Referee is a static object that provides functions to run a game of Q.

Handles local Players that may throw exceptions.
Does not yet support remote players that may have other failure modes (timeout, disconnect).
*/
object Referee {

    /*
    Runs a Q game from a provided set of Players and a preexisting game state.
    Assumes 'players' is provided in the correct play order, i.e. players(0) is the next to play.

    @return A GameResult including the players who won the game and the players who misbehaved.
    */
    def play(players: List[Player], cfg: RefereeConfig): GameResult = {
        cfg.state.setScoringConfig(cfg.stateConfig)
        val result = accPlay(players, cfg.state, List[String](), cfg.observer)
        notifyWinners(players, result.winners)
        result
    }

    /*
    Plays a game of Q, accumulating the misbehaved players round by round.
    Guaranteed to terminate because the function returns when there are no players or a round ends the game.
    */
    private def accPlay(players: List[Player], state: GameState, misbehaved: List[String], observer: Option[Observer]): GameResult = {
        players match {
            case Nil => {
                val result = GameResult(state.getWinners, misbehaved)
                observer.foreach(_.gameOver(result))
                result
            }
            case head :: next => {
                val round = handleRound(head :: next, state, observer)
                if (round.endsGame) {
                    val result = GameResult(state.getWinners, misbehaved ++ round.misbehaved)
                    observer.foreach(_.gameOver(result))
                    return result
                } else {
                    val nextRoundPlayers = players.filter(player => !round.misbehaved.contains(player.name))
                    accPlay(nextRoundPlayers, state, misbehaved ++ round.misbehaved, observer)
                }
            }
        }
    }

    /*
    Plays a single round of Q and returns a RoundResult. 
    Assumes that no placement turns in a round or a player placing all tiles ends the game.
    */
    private def handleRound(players: List[Player], state: GameState, observer: Option[Observer]): RoundResult = {
        var misbehaved: List[String] = Nil
        var atLeastOnePlacement: Boolean = false
        for (player <- players) {
            handleTurn(player, state) match {
                case TurnResult.MISBEHAVED => misbehaved = player.name :: misbehaved
                case TurnResult.PLACED => atLeastOnePlacement = true
                case TurnResult.PLACEDALL => return RoundResult(true, misbehaved.reverse)
                case TurnResult.NOTPLACED =>
            }
            val copiedState = state.copy
            observer.foreach(_.receiveState(copiedState))
        }
        RoundResult(!atLeastOnePlacement, misbehaved.reverse)
    }

    /*
    Takes the active player and game state and handles the active player's turn, 
    returning a TurnResult indicating if the player misbehaved, placed tiles, and placed all its tiles, respectively.
    Catches Player exceptions, marks them as misbehaved, and kicks them.
    */
    private def handleTurn(active: Player, state: GameState): TurnResult = {
        val info = state.getTurnInfo
        var action: Action = PassAction
        try {
            action = active.takeTurn(info)
        } catch {
            case e: Exception => {
                state.removeActivePlayer
                return TurnResult.MISBEHAVED
            }
        }

        if (state.isValidAction(action)) {
            val tiles = state.executeAction(action)
            try {
                active.newTiles(tiles)
            } catch {
                case e: Exception => {
                    state.removeActivePlayer
                    return TurnResult.MISBEHAVED
                }
            }
            action match {
                case PlaceAction(placements) => {
                    if (placements.size == info.player.tiles.size) return TurnResult.PLACEDALL
                    else return TurnResult.PLACED
                }
                case _ => {
                    return TurnResult.NOTPLACED
                }
            }
        } else {
            state.removeActivePlayer
            return TurnResult.MISBEHAVED
        }
    }

    /*
    Notifies players whether they won or lost.
    */
    private def notifyWinners(players: List[Player], winners: List[String]): Unit = {
        for (player <- players) {
            try {
                if (winners.contains(player.name)) player.win(true)
                else player.win(false)
            } catch {
                case e: Exception => 
            }
        }
    }

    /*
    Generates and allocates tiles to a list of PlayerStates that correspond to incoming Players.

    Assumes Players is sorted in ascending age order and generates fake ages accordingly, 
    returning PlayerStates in the reverse order.
    */
    private def createPlayerStates(tiles: List[Tile], players: List[Player], map: MapMap): List[PlayerState] = {

        if (tiles.size != Constants.NUM_START_TILE * players.size) {
            throw new IllegalArgumentException("Bad number of tiles provided.")
        }
        var playerStates = List[PlayerState]()
        var acc = 0
        for (player <- players) {
            val playerTiles = tiles.slice(acc * Constants.NUM_START_TILE, (acc + 1) * Constants.NUM_START_TILE)
            //TODO new definition of setup breaks this
            // setup needs to be called with player-specific state
            //player.setup(TurnInfo(), playerTiles)
            playerStates = new PlayerState(playerTiles, acc, player.name, 0) :: playerStates
            acc += 1
        }
        return playerStates
    }

    /*
    Generates the list of all tiles in the Q game.
    */
    private def generateRefereeTiles: List[Tile] = {
        var tiles = List[Tile]()
        for (color <- Color.values) {
            for (shape <- Shape.values) {
                for (_ <- 1 to Constants.NUM_EACH_TILE) {
                    tiles = Tile(color, shape) :: tiles
                }
            }
        }
        return tiles
    }

}