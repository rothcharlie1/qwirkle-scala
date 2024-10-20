package xstrategy

import scala.io.StdIn.readLine
import spray.json._
import common.Placement
import common.QSerializer
import player.strategy.Strategy
import player.Action
import player.ExchangeAction
import player.PassAction
import player.PlaceAction

/*
Collects a JPub and a JStrategy from STDIN and returns the next JAction requested by the strategy given its public info.
*/
object XStrategy {
    
    def main(args: Array[String]): Unit = QSerializer.getNextJson match {
        case Some(jpub) => {
            val info = QSerializer.deserializeJPub(jpub)
            QSerializer.getNextJson match {
                case Some(jstrategy) => {
                    val strategy: Strategy = QSerializer.deserializeJStrategy(jstrategy)
                    val message = strategy.produceAction(info) match {
                        case ExchangeAction => JsString("replace")
                        case PassAction => JsString("pass")
                        case PlaceAction(placements) => placements match {
                            case plmt :: Nil => QSerializer.serialize1Placement(plmt)
                            case _ => throw new IllegalArgumentException("Strategy did not return correct number of actions")
                        }
                    }
                    QSerializer.sendJson(message)
                }
                case None => throw new IllegalArgumentException("Bytes available in stdin were not JSON.")
            }
        }    
        case None => throw new IllegalArgumentException("Bytes available in stdin were not JSON.")
    }
}