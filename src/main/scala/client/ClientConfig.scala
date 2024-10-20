package client

import player.Player
import common.QSerializer
import spray.json._

/*
The configuration required to connect a set of clients to a Q game.
*/
case class ClientConfig(port: Int, host: String, waiting: Int, quiet: Boolean, players: List[Player])

object ClientConfig {

    /*
    Deserialize JSON into a ClientConfig
    */
    def fromJClientConfig(jclient: JsValue): ClientConfig = jclient match {
        case JsObject(fields) => {
            fields.get("port") match {
                case Some(JsNumber(port)) => {
                    fields.get("host") match {
                        case Some(JsString(host)) => {
                            fields.get("wait") match {
                                case Some(JsNumber(waiting)) => {
                                    fields.get("quiet") match {
                                        case Some(JsBoolean(quiet)) => {
                                            fields.get("players") match {
                                                case Some(jactors) => {
                                                    val players = QSerializer.deserializeJActors(jactors)
                                                    ClientConfig(port.toInt, host, waiting.toInt, quiet, players)
                                                }
                                                case _ => throw new Exception("Improper 'players' field.")
                                            }
                                        }
                                        case _ => throw new Exception("Improper 'quiet' field.")
                                    }
                                }
                                case _ => throw new Exception("Improper 'wait' field.")
                            }
                        }
                        case _ => throw new Exception("Improper 'host' field.")
                    }
                }
                case _ => throw new Exception("Improper 'port' field.")
            }
        }
        case _ => throw new Exception("ClientConfig was not a JSON object.")
    }
}