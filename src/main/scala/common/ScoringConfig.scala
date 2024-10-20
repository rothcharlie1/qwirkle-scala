package common

import spray.json._

case class ScoringConfig(qBonus: Int, finishBonus: Int)

object ScoringConfig {

    def fromRefereeStateConfig(cfg: JsValue): ScoringConfig = cfg match {
        case JsObject(fields) => {
            val qBonus = fields.get("qbo") match {
                case Some(JsNumber(num)) => num
                case _ => throw new Exception("qbo field was not present or not a number.")
            }
            val finishBonus = fields.get("fbo") match {
                case Some(JsNumber(num)) => num
                case _ => throw new Exception("fbo field not present or not a number.")
            }
            ScoringConfig(qBonus.toInt, finishBonus.toInt)
        }
        case _ => throw new Exception("RefereeStateConfig was not a JSON object.")
    }
}