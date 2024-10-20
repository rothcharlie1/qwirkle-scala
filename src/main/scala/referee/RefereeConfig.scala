package referee

import common.GameState
import common.ScoringConfig
import common.QSerializer
import spray.json._

/*
A RefereeConfig is a configuration for running a Referee.
@param state The GameState to start a Referee from.
@param secondsPerCall An Int representing the number of seconds to wait for each player call to return.
@param observer An optional observer to attach to the game.
*/
case class RefereeConfig(state: GameState, stateConfig: ScoringConfig, secondsPerCall: Int, observer: Option[Observer])

object RefereeConfig {

    def fromJRefereeConfig(cfg: JsValue): RefereeConfig = cfg match {
        case JsObject(fields) => {
            fields.get("state0") match {
                case Some(jstate) => {
                    val state = QSerializer.deserializeJState(jstate)
                    fields.get("quiet") match {
                        case Some(JsBoolean(quiet)) => {
                            fields.get("config-s") match {
                                case Some(jscorecfg) => {
                                    val scoringConfig = ScoringConfig.fromRefereeStateConfig(jscorecfg)
                                    fields.get("per-turn") match {
                                        case Some(JsNumber(secondsPerCall)) => {
                                            val observer = fields.get("observe") match {
                                                case Some(JsBoolean(true)) => Some(new QObserver())
                                                case Some(JsBoolean(false)) => None 
                                                case _ => throw new Exception("'observe' field malformed or missing")
                                            }
                                            RefereeConfig(state, scoringConfig, secondsPerCall.toInt, observer)
                                        }
                                        case _ => throw new Exception("Malformed 'per-turn'.")
                                    }
                                }
                                case None => throw new Exception("Malformed 'config-s'.")
                            }
                        }
                        case _ => throw new Exception("Malformed 'quiet' field.")
                    }
                }
                case _ => throw new Exception("Malformed state0 field.")
            }
        }
        case _ => throw new Exception("Referee config was not a JSON object")
    }
}