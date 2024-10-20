package referee

import spray.json._
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.nio.file._

import common.GameState

/*
Defines the required functionality for Observers of the Q game and provides
general save functions Observers can use.
*/
trait Observer {

    /*
    Receives an updated GameState from the Referee for this Observer to use
    */
    def receiveState(state: GameState): Unit

    /*
    Signals to this Observer that the game being Observed is over
    */
    def gameOver(result: GameResult): Unit

    def saveStateImage(state: GameState, filename: String): Unit = {
        val rendered: BufferedImage = state.render
        ImageIO.write(rendered, "png", new File(filename))
    }

    def saveStateJSON(state: GameState, filename: String): Unit = {
        val jstate = state.toJState
        val filePath = Paths.get(filename)
        Files.write(filePath, jstate.prettyPrint.getBytes)
    }
}