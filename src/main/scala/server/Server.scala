package server

import java.net.{ServerSocket, Socket}
import scala.io.BufferedSource
import scala.concurrent.duration._
import comms.{CommunicationUtils => comms}
import referee.GameResult
import referee.Referee

class Server(private val cfg: ServerConfig) {
    private val serverSocket = new ServerSocket(cfg.port)

    /*
    Collects a list of players, and, if possible, plays a game with them and returns the result.
    Returns an empty GameResult if no game is played.
    */
    def start: GameResult = {
        val players = generatePlayers(Nil, cfg.numSignupPeriods)
        if (players.size < 2) GameResult(Nil, Nil)
        else Referee.play(players, cfg.refConfig)
    }

    /*
    Recur over the number of waiting periods and generate players while there are still remaining waiting periods
    and there are less than 4 players.
    */
    private def generatePlayers(players: List[ProxyPlayer], remainingPeriods: Int): List[ProxyPlayer] = remainingPeriods match {
        case 0 => players
        case period: Int => {
            var tempPlayers = players
            val endOfPeriod = System.currentTimeMillis() + cfg.signupPeriodSeconds.seconds.toMillis
            while (System.currentTimeMillis() < endOfPeriod && tempPlayers.size < 4) {
                val socket = serverSocket.accept() // can run infinitely, add a timeout
                comms.receiveNext(socket, cfg.nameSubmissionDelaySeconds.seconds) match {
                    case Some(name) => {
                        val proxyPlayer = new ProxyPlayer(socket, name, cfg.refConfig.secondsPerCall)
                        tempPlayers = proxyPlayer :: tempPlayers
                    }
                    case None => socket.close()
                }
            }
            if (tempPlayers.size == 4) tempPlayers
            else generatePlayers(tempPlayers, period - 1)
        }
    }   
}