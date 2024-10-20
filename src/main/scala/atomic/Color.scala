package atomic

import java.awt.{Color => AwtColor}

/*
Enumeration of the Q Game tile colors.
*/
object Color extends Enumeration{
    type Color = Value
    val RED = Value("red")
    val GREEN = Value("green")
    val BLUE = Value("blue")
    val YELLOW = Value("yellow")
    val ORANGE = Value("orange")
    val PURPLE = Value("purple")

    def hasOneOfEach(colors: List[Color]): Boolean = {
        colors.size == Color.values.size && colors.toSet == Color.values.toSet
    }

    def toAwtColor(color: Color): AwtColor = {
        color match {
            case RED => AwtColor.RED
            case GREEN => AwtColor.GREEN
            case BLUE => AwtColor.BLUE
            case YELLOW => AwtColor.YELLOW
            case ORANGE => AwtColor.ORANGE
            case PURPLE => new AwtColor(128, 0, 128)
        }
    }
}    
