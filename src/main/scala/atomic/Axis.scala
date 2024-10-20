package atomic

import Direction.Direction

/*
A dimension of a board, represented as a collection of Directions.
*/
case class Axis(dirs: List[Direction])

object Axis {
    val ROW = Axis(List(Direction.LEFT, Direction.RIGHT))
    val COLUMN = Axis(List(Direction.UP, Direction.DOWN))
}