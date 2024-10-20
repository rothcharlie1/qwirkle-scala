package xscore

import common._
import scala.io.StdIn.readLine
import spray.json._
import common.Placement

object XScore {
    
    def main(args: Array[String]): Unit = {
        while (System.in.available() != 0) {
            
            getNextJson match {
                case Some(jmap) => {
                    var mapMap = QSerializer.deserializeJMap(jmap)
                    getNextJson match {
                        case Some(jplacements) => {
                            var placements: List[Placement] = QSerializer.deserializeJPlacements(jplacements)
                            var gameState = new GameState(mapMap, List(new PlayerState(placements.map(_.tile), 99, "_", 0)))
                            println(JsNumber(gameState.scorePlacement(placements)).prettyPrint)
                        }
                        case None => throw new IllegalArgumentException("Bytes available in stdin were not JSON.")
                    }
                }    
                case None => throw new IllegalArgumentException("Bytes available in stdin were not JSON.")
            }
            
            
        }
    }

    def getNextJson: Option[JsValue] = {
        var line = scala.io.StdIn.readLine()
        var currArr = ""

        while (line != null && line.nonEmpty) {
            currArr += line
            try {
                // check if current array is complete JSON
                // this flow control is dependent on the following truth: valid JSON + one extra line is not valid JSON,
                // i.e. the JSON is complete as soon as it is valid
                val json = currArr.parseJson
                return Some(json)
            } catch {
                case e: JsonParser.ParsingException =>
                // this case is ok, we continue trying to build a complete array
                case e: RuntimeException => throw e
            } 
        }
        return None
    }
    
}