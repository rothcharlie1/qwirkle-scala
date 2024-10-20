package server

import referee.RefereeConfig
import spray.json._

case class ServerConfig(
    port: Int, 
    numSignupPeriods: Int, 
    signupPeriodSeconds: Int,
    nameSubmissionDelaySeconds: Int,
    quiet: Boolean,
    refConfig: RefereeConfig)

object ServerConfig {

    def fromJServerConfig(cfg: JsValue): ServerConfig = cfg match {
        case JsObject(fields) => {
            fields.get("port") match {
                case Some(JsNumber(port)) => {
                    fields.get("server-tries") match {
                        case Some(JsNumber(tries)) => {
                            fields.get("server-wait") match {
                                case Some(JsNumber(waiting)) => {
                                    fields.get("wait-for-signup") match {
                                        case Some(JsNumber(nameDelay)) => {
                                            fields.get("quiet") match {
                                                case Some(JsBoolean(quiet)) => {
                                                    fields.get("ref-spec") match {
                                                        case Some(refcfg) => {
                                                            val refConfig = RefereeConfig.fromJRefereeConfig(refcfg)
                                                            ServerConfig(port.toInt, tries.toInt, waiting.toInt, nameDelay.toInt, quiet, refConfig)
                                                        }
                                                        case _ => throw new Exception("ref-spec field malformed.")
                                                    }
                                                }
                                                case _ => throw new Exception("Improper 'quiet' field.")
                                            }
                                        }
                                        case _ => throw new Exception("Improper 'wait-for-signup' field.")
                                    }
                                }
                                case _ => throw new Exception("Improper 'server-wait' field.")
                            }
                        }
                        case _ => throw new Exception("Improper 'server-tries' field.")
                    }
                }
                case _ => throw new Exception("Improper 'port' field.")
            }
        }
        case _ => throw new Exception("ClientConfig was not a JSON object.")
    }
}