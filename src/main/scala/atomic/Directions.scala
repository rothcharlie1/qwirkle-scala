package atomic

/*
Represents a cardinal direction on the board.
*/
object Direction extends Enumeration {
    type Direction = Value

    val UP, DOWN, LEFT, RIGHT = Value;
}