package referee

import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.{Color => AwtColor}
import java.awt.Font
import spray.json._
import scala.math.max

/*
The result of a game of Q.
*/
case class GameResult(winners: List[String], misbehaved: List[String]) {

    def render: Image = {
        val image = new BufferedImage(200, (max(winners.size, misbehaved.size) + 2) * 50, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()
        g2d.setColor(AwtColor.WHITE)
        println(image.getHeight(null))
        g2d.fillRect(0, 0, image.getWidth(null), image.getHeight(null))
        g2d.setColor(AwtColor.BLACK)
        g2d.setFont(new Font("Arial", Font.BOLD, 12))

        // draw label
        g2d.drawString("Winners:", 30, 50)
        g2d.drawString("Misbehaved:", 130, 50)

        for (i <- 1 to winners.size) {
            g2d.drawString(winners(i-1), 30, (i * 50) + 50)
        }
        for (i <- 1 to misbehaved.size) {
            g2d.drawString(misbehaved(i-1), 130, (i * 50) + 50)
        }
        g2d.dispose()
        image
    }

    def toJGameResult: JsArray = {
        val jwinners = JsArray(winners.sorted.map(JsString(_)))
        val jmisbehaved = JsArray(misbehaved.map(JsString(_)))
        JsArray(List(jwinners, jmisbehaved))
    }
}