package common

import org.scalatest.flatspec.AnyFlatSpec
import atomic.Color._ 
import atomic.Shape._ 


class TileSpec extends AnyFlatSpec {

    val redSquare: Tile = Tile(RED, SQUARE)

    behavior of "A red square Tile"

    it should "match a red circle Tile" in {
        assert(redSquare.matches(Some(Tile(RED, CIRCLE))) == true)
    }

    it should "match a purple square Tile" in {
        assert(redSquare.matches(Some(Tile(PURPLE, SQUARE))) == true)
    }

    it should "not match a green star Tile" in {
        assert(redSquare.matches(Some(Tile(GREEN, STAR))) == false)
    }
}