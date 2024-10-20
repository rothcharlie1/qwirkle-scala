package atomic

/*
Enumeration of the Q Game tile shapes.
*/
object Shape extends Enumeration {
    type Shape = Value
    val STAR = Value("star")
    val EIGHTSTAR = Value("8star")
    val SQUARE = Value("square")
    val CIRCLE = Value("circle")
    val CLOVER = Value("clover")
    val DIAMOND = Value("diamond")

    def hasOneOfEach(shapes: List[Shape]): Boolean = {
        shapes.size == Shape.values.size && shapes.toSet == Shape.values.toSet
    }
}

