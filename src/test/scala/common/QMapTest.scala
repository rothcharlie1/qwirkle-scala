package common

import org.scalatest.flatspec.AnyFlatSpec
import atomic.Posn
import atomic.Color._ 
import atomic.Shape._


class MapMapSpec extends AnyFlatSpec {

    val singleTileMap = new MapMap(Some(Tile(RED, SQUARE)))

    behavior of "A map with a single center RED SQUARE tile"

    it should "provide 4 locations to insert a RED CIRCLE tile" in {
        assert(singleTileMap.getValidTileInsertions(Tile(RED, CIRCLE)).length == 4)
    }

    it should "allow placement of a RED CIRCLE at (0,1)" in {
        singleTileMap.insertTile(Tile(RED, CIRCLE), Posn(0,1))
        singleTileMap.getTileAtPosition(Posn(0, 1)) match {
            case Some(tile) => assert(tile == Tile(RED, CIRCLE))
            case None => assert(false)
        }
    }

    it should "then allow placement of a GREEN CIRCLE at (-1, 1)" in {
        singleTileMap.insertTile(Tile(GREEN, CIRCLE), Posn(-1, 1))
        singleTileMap.getTileAtPosition(Posn(-1, 1)) match {
            case Some(tile) => assert(tile == Tile(GREEN, CIRCLE))
            case None => assert(false)
        }
    }

    it should "then block insertion of an ORANGE SQUARE at (2, 2)" in {
        assertThrows[IllegalArgumentException] {
            singleTileMap.insertTile(Tile(ORANGE, SQUARE), (Posn(2, 2)))
        }
    }

    behavior of "A map with a gap only for a GREEN STAR"

    it should "allow insertion of a GREEN STAR at (1,0)" in {
        var map = new MapMap(Some(Tile(GREEN, CIRCLE)))
        map.insertTile(Tile(RED, CIRCLE), Posn(0, 1))
        map.insertTile(Tile(RED, STAR), Posn(1, 1))
        map.insertTile(Tile(ORANGE, DIAMOND), Posn(2, 1))
        map.insertTile(Tile(GREEN, DIAMOND), Posn(2, 0))
        map.insertTile(Tile(GREEN, STAR), Posn(1, 0))
        map.getTileAtPosition(Posn(1, 0)) match {
            case Some(tile) => assert(tile == Tile(GREEN, STAR))
            case None => assert(false)
        }
    }

    it should "not suggest (0,1) as a place for a GREEN DIAMOND" in {
        var map = new MapMap(Some(Tile(GREEN, CIRCLE)))
        map.insertTile(Tile(RED, CIRCLE), Posn(0, 1))
        map.insertTile(Tile(RED, STAR), Posn(1, 1))
        map.insertTile(Tile(ORANGE, DIAMOND), Posn(2, 1))
        map.insertTile(Tile(GREEN, DIAMOND), Posn(2, 0))
        assert(map.getValidTileInsertions(Tile(GREEN, DIAMOND)).contains(Posn(0,1)) == false)
    }
}