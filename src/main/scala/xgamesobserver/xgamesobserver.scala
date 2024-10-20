package xgamesobserver


import scala.io.StdIn.readLine
import spray.json._
import common.Placement
import common.QSerializer
import player.strategy.Strategy
import player.Action
import player.ExchangeAction
import player.PassAction
import player.PlaceAction
import referee.Referee
import referee.Observer
import referee.QObserver
import referee.RefereeConfig
import common.ScoringConfig

/*
Collects a JState and a JActors from STDIN and returns the result of running a full game on the state with the actors.
*/
object XGamesWithObserver {
    
    def main(args: Array[String]): Unit = {
        val jstate = QSerializer.getNextJson.get
        val players = QSerializer.deserializeJActors(QSerializer.getNextJson.get)
        val gamestate = QSerializer.deserializeJState(jstate)
        var observer: Option[Observer] = None
        args.lift(0) match { 
            case Some(value) if value == "-show" => {
                observer = Some(new QObserver())
            }
            case _ =>
        }
        val result = Referee.play(players, RefereeConfig(gamestate, ScoringConfig(8,4) ,6, observer))
        QSerializer.sendJson(QSerializer.serializeGameResult(result))
    }
}