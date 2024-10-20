package common

import spray.json._
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.{Color => AwtColor}
import atomic.Color
import atomic.Shape
import atomic.Color.Color
import atomic.Shape.Shape
import spray.json._

/* 
A case class representing a tile in the Q Game.
Takes advantage of the Scala "case class", designed for immutable data structures.
*/
case class Tile(val color: Color, val shape: Shape) extends Ordered[Tile] {

    /*
    Allows correct Tile sorting using the notion of 'smaller'.
    */
    def compare(that: Tile): Int = {
        val shapes = Shape.values.toList
        val colors = Color.values.toList
        shapes.indexOf(shape).compare(shapes.indexOf(that.shape)) match {
            case x if x < 0 => -1
            case x if x > 0 => 1
            case _ => colors.indexOf(color).compare(colors.indexOf(that.color))
        }
    }

    override def toString: String = color.toString + " " + shape.toString

    /*
    Checks if the given object matches either the Color or Shape, or both, of this Tile.

    @param obj An object to check for a Tile match
    @return Whether a match exists.
    */
    def matches(other: Option[Tile]): Boolean = {
        other match {
            case Some(that) => (this.color eq that.color) || (this.shape eq that.shape)
            case None => true
        }
    }

    def toJTile: JsValue = {
        JsObject(Map("color" -> JsString(color.toString), "shape" -> JsString(shape.toString)))
    }

    def render: BufferedImage = {
        val image = new BufferedImage(util.Constants.TILE_SIZE, util.Constants.TILE_SIZE, BufferedImage.TYPE_INT_ARGB)

        val g2d: Graphics2D = image.createGraphics()

        g2d.setColor(AwtColor.WHITE)
        g2d.fillRect(0,0,util.Constants.TILE_SIZE, util.Constants.TILE_SIZE)

        g2d.setColor(atomic.Color.toAwtColor(color))

        shape match {
            case atomic.Shape.SQUARE => g2d.fillRect(0, 0, util.Constants.TILE_SIZE, util.Constants.TILE_SIZE)
            case atomic.Shape.CIRCLE => g2d.fillOval(0, 0, util.Constants.TILE_SIZE, util.Constants.TILE_SIZE)
            case atomic.Shape.CLOVER => {
                g2d.fillOval(0, util.Constants.TILE_SIZE / 4, util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 2)
                g2d.fillOval(util.Constants.TILE_SIZE / 4, 0, util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 2)
                g2d.fillOval(util.Constants.TILE_SIZE / 4, util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 2)
                g2d.fillOval(util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 4, util.Constants.TILE_SIZE / 2, util.Constants.TILE_SIZE / 2)
            }
            case _ => {
                val xPoints = util.Constants.xPointsMap(shape)
                val yPoints = util.Constants.yPointsMap(shape)
                g2d.fillPolygon(xPoints, yPoints, xPoints.length)
            }
        }
        g2d.dispose()
        image 
    }
}

object Tile {

    def fromJTile(jtile: JsValue): Tile = jtile match {
        case JsObject(fields) => {
            fields.get("shape") match {
                case Some(JsString(shapeStr)) => {
                    val shape: Shape = atomic.Shape.withName(shapeStr)
                    fields.get("color") match {
                        case Some(JsString(colorStr)) => {
                            val color: Color = atomic.Color.withName(colorStr)
                            return Tile(color, shape)
                        }
                        case _ => throw new IllegalArgumentException("JTile did not contain 'color' field.")
                    }
                }
                case _ => throw new IllegalArgumentException("JTile did not contain 'shape' field.")
            }
        }
        case _ => throw new IllegalArgumentException("JTile was not a JSON Object.")
    }
}