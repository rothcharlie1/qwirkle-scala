package common

import java.awt.Image
import atomic.Posn

/*
Trait describing core functionality a map for the Q Game must implement.
*/
trait QMap {

    /*
    Inserts the provided Tile at position (x,y) if (x,y) has a neighbor tile.

    @param tile The Tile to insert.
    @param x The Posn position to place the Tile at.
    @throws IllegalArgumentException if a tile is already in the given position
    @throws IllegalArgumentException if the desired position has no neighbor tile.
    */
    def insertTile(tile: Tile, pos: Posn): Unit

    /*
    Provides a list of all Posns the given Tile could be legally placed in.

    @param tile The Tile that would be inserted.
    @return A list of Posns that 'tile' can be placed in.
    */
    def getValidTileInsertions(tile: Tile): List[Posn]

    /*
    Returns the tile at the provided position or None.

    @param pos The Posn in question.
    @return The tile at (x,y) or None if it does not exist.
    */
    def getTileAtPosition(pos: Posn): Option[Tile]

    /*
    Returns a rectangular Image showing the board of tiles and the gaps between them as whitespace.
    */
    def render: Image
}