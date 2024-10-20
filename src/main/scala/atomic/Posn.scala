package atomic

import Direction.Direction
import atomic.Axis
import spray.json._

/*
Represents a position on a game board with integer Cartesian coordinates.
*/
class Posn(val x: Int, val y: Int) extends Ordered[Posn] {

    /*
    Compares this Posn to another Posn using row-column priority.
    Required as an extension of Ordered[Posn].
    */
    def compare(that: Posn): Int = {
        if (this.equals(that)) return 0
        else if (this.y > that.y || (this.y == that.y && this.x < that.x)) {
            return -1
        } else {
            return 1
        }
    }

    override def hashCode: Int = (x,y).hashCode

    override def equals(obj: Any): Boolean = {
        obj match {
            case that: Posn => this.x == that.x && this.y == that.y
            case _ => false
        }
    }

    def inDirection(dir: Direction): Posn = {
        dir match {
            case Direction.LEFT => Posn(x - 1, y)
            case Direction.RIGHT => Posn(x + 1, y)
            case Direction.UP => Posn(x, y + 1)
            case Direction.DOWN => Posn(x, y - 1)
        }
    }

    override def toString: String = s"X: $x Y: $y"

    /*
    Provides the neighbors along a given Axis.
    */
    def neighborsAlongAxis(axis: Axis): List[Posn] = axis.dirs.map(inDirection(_))

    def neighbors: List[Posn] = Direction.values.map(inDirection(_)).toList

    /*
    Checks if this Posn and another are in the same row.
    */
    def sameRow(that: Posn): Boolean = this.y == that.y

    /*
    Checks if this Posn and another are in the same column.
    */
    def sameCol(that: Posn): Boolean = this.x == that.x

    def allInSameRow(others: List[Posn]): Boolean = {
        others.forall(sameRow(_))
    }

    def allInSameCol(others: List[Posn]): Boolean = {
        others.forall(sameCol(_))
    }

    def isAbove(pos: Posn): Boolean = this.y > pos.y
    def isBelow(pos: Posn): Boolean = this.y < pos.y
    def isLeft(pos: Posn): Boolean = this.x < pos.x
    def isRight(pos: Posn): Boolean = this.x > pos.x

    def subtract(pos: Posn): Posn = {
        Posn(this.x - pos.x, this.y - pos.y)
    }

    def toJCoordinate: JsValue = JsObject(Map(
        "row" -> JsNumber(-1 * y),
        "column" -> JsNumber(x)
    ))
}

/*
Companion Posn object allowing instantiation of Posn without 'new' keyword.
*/
object Posn {
    def apply(x: Int, y: Int): Posn = new Posn(x, y)

    def fromJCoordinate(jcoord: JsValue): Posn = jcoord match {
        case JsObject(fields) => {
            fields.get("row") match {
                case Some(JsNumber(row)) => {
                    fields.get("column") match {
                        case Some(JsNumber(col)) => {
                            Posn(col.toInt, -1 * row.toInt)
                        }
                        case _ => throw new IllegalArgumentException("Badly formed JCoordinate.")
                    }
                }
                case _ => throw new IllegalArgumentException("Badly formed JCoordinate.")
            }

        }
        case _ => throw new IllegalArgumentException("JCoordinate was not a JSON object.")
    }
}