package common

import spray.json._
import java.awt.image.BufferedImage
import java.awt.Image
import java.awt.Graphics2D
import scala.collection.mutable.{Map => MutMap}
import atomic.Direction.Direction
import atomic.Axis
import atomic.Direction
import atomic.Posn
import util.Constants.TILE_SIZE

//remove these after testing
import javax.imageio.ImageIO
import java.io.File

/*
An implementation of QMap that uses a Map[Posn -> Tile] to store the grid.
*/
class MapMap(originTile: Option[Tile]) extends QMap {

    // representation of the game grid as a mutable hashmap that enforces tile contiguity
    private var grid: Map[Posn, Tile] = Map[Posn, Tile]()
    originTile match {
        case Some(tile) => grid = Map(Posn(0,0) -> tile)
        case None =>
    }

    /*
    Builds a MapMap from a preexisting Map[Posn, Tile].
    */
    def this(map: Map[Posn, Tile]) = {
        this(None)
        map.get(Posn(0,0)) match {
            case Some(tile) => this.grid = this.grid + (Posn(0,0) -> tile)
            case None => throw new IllegalArgumentException("Attempted to construct a multi-Tile map with no origin Tile.")
        }
        for ((pos, tile) <- map) {
            this.grid = this.grid + (pos -> tile)
        }
    }

    /*
    Inserts the provided Tile at position pos if pos has a neighbor tile.

    @param tile The Tile to insert.
    @param pos The Posn to insert the tile at.
    @throws IllegalArgumentException if a tile is already in the given position
    @throws IllegalArgumentException if the desired position has no neighbor tile.
    */
    def insertTile(tile: Tile, pos: Posn): Unit = {
        if (hasTileNeighbor(pos)) {
            this.grid.get(pos) match {
                case Some(tile) => throw new IllegalArgumentException("Requested position already has a tile.")
                case None => this.grid = this.grid + (pos -> tile)
            }
        } else throw new IllegalArgumentException("Desired position has no neighboring tiles.")
    }

    /*
    Provides a list of all (x,y) positions the given Tile could be legally placed in.

    @param tile The Tile that would be inserted.
    @return A list of Posns that 'tile' can be placed in.
    */
    def getValidTileInsertions(tile: Tile): List[Posn] = {
        getSingleMatchPositions(tile)
            .filter(!hasTile(_))
            .filter(fits(tile, _))
    }

    def numTilesPlaced: Int = this.grid.size

    /*
    Returns the tile at the provided position or None.

    @param pos The Posn at which to get the Tile
    @return The tile at (x,y) or None if it does not exist.
    */
    def getTileAtPosition(pos: Posn): Option[Tile] = this.grid.get(pos)

    /*
    Finds all contiguous sequences in the map (both horizontal and vertical).
    */
    def findContiguousSequencesFromPosition(pos: Posn): List[Set[Posn]] = {
        List(
            findContiguousSequenceAlongAxis(pos, atomic.Axis.ROW).toSet, 
            findContiguousSequenceAlongAxis(pos, atomic.Axis.COLUMN).toSet
        ).filter(s => !(s.size == 0))
    }

    /*
    Finds all contiguous sequences from a given position along the specified axis.
    */
    private def findContiguousSequenceAlongAxis(pos: Posn, axis: Axis): List[Posn] = {
        axis.dirs.map(findContiguousSequenceInDirection(_, pos)).flatten match {
            case Nil => Nil 
            case head :: tail => pos :: head :: tail
        }
    }

    /*
    Finds the largest contiguous sequence from a given Posn in a single Direction
    */
    private def findContiguousSequenceInDirection(dir: Direction, pos: Posn): List[Posn] = {
        val neighbor = pos.inDirection(dir)
        getTileAtPosition(neighbor) match {
            case Some(_) => neighbor :: findContiguousSequenceInDirection(dir, neighbor)
            case None => Nil
        }
    }

    /*
    Provide an immutable copy of the current game board hashmap.
    */
    def getMap: Map[Posn, Tile] = this.grid.map{ case (key, value) => key -> value}

    /*
    Copies this MapMap object to a new MapMap.
    */
    def copy: MapMap = new MapMap(this.grid)

    /*
    Checks if the provided tile matches the immediate neighbors of the provided position.

    @param tile The Tile that would be inserted.
    @param pos The Posn to check for tile compatibility.
    @return true if 'tile' matches the immediate neighbors of 'pos', else false.
    */
    def fits(tile: Tile, pos: Posn): Boolean = {
        matchesAxis(tile, pos, atomic.Axis.ROW) && matchesAxis(tile, pos, atomic.Axis.COLUMN)
    }

    /*
    Checks if the provided tile matches the tiles neighboring the provided position along the given axis.

    @param tile The Tile that would be inserted.
    @param pos The Posn to check for tile compatibility.
    @param axis The axis along which to check.
    @return true if the tile matches along the axis, else false.
    */
    private def matchesAxis(tile: Tile, pos: Posn, axis: Axis): Boolean = {
        val tiles = pos.neighborsAlongAxis(axis).map(getTileAtPosition(_))
        if (tiles.forall(tile.matches(_)) == false) return false

        // If either neighbor tile is None, return true. Else, check if they match each other.
        tiles match {
            case neighborTile :: tail => {
                neighborTile match {
                    case Some(tile: Tile) => tail.forall(tile.matches(_))
                    case None => true
                } 
            }
            case Nil => true
        }
    }

    /*
    Finds all positions with at least one neighbor that would match the provided tile.

    @param tile The Tile to check compatibility with.
    @return A list of positions that have at least one immediate neighbor that matches 'tile' by Color or Shape.
    */
    private def getSingleMatchPositions(tile: Tile): List[Posn] = {
        var matches: Set[Posn] = Set[Posn]()
        for ((pos, gridTile) <- this.grid) {
            if (gridTile.matches(Some(tile))) {
                matches = matches ++ pos.neighborsAlongAxis(atomic.Axis.ROW) ++ pos.neighborsAlongAxis(atomic.Axis.COLUMN)
            } 
        }
        return matches.toList
    }

    /*
    Checks if the provided Posn holds a tile.
    */
    def hasTile(pos: Posn): Boolean = {
        this.grid.get(pos) match {
            case None => false
            case Some(_) => true
        }
    }

    /*
    Checks if the provided position has at least one neighboring tile.

    @param pos Posn of the position in question
    @return true if there is a neighboring tile, false otherwise.
    */
    def hasTileNeighbor(pos: Posn): Boolean = {
        for (neighbor: Posn <- pos.neighborsAlongAxis(atomic.Axis.ROW) ++ pos.neighborsAlongAxis(atomic.Axis.COLUMN)) {
            this.grid.get(neighbor) match {
                case Some(tile) => return true
                case None =>
            }
        }
        return false
    }

    def toJMap: JsValue = {
        var rowLists: MutMap[Int, List[List[JsValue]]] = MutMap[Int, List[List[JsValue]]]()
        for ((posn, tile) <- grid) {
            var jcell = List(JsNumber(posn.x), tile.toJTile)
            rowLists.get(-1 * posn.y) match {
                case Some(elements) => {
                    rowLists(-1 * posn.y) = jcell :: rowLists(-1 * posn.y)
                }
                case None => rowLists(-1 * posn.y) = jcell :: Nil
            }
        }
        var jrows: List[JsArray] = Nil
        for ((rowIdx, jcells) <- rowLists) {
            jrows = JsArray(JsNumber(rowIdx) :: jcells.map(JsArray(_))) :: jrows
        }
        JsArray(jrows)
    }

    /*
    Returns a rectangular Image showing the board of tiles and the gaps between them as whitespace.
    */
    def render: Image = {

        var maxX = Posn(0,0)
        var maxY = Posn(0,0)
        var minX = Posn(0,0)
        var minY = Posn(0,0)

        for ((pos, tile) <- this.grid) {
            if (pos.isAbove(maxY)) maxY = pos
            if (pos.isBelow(minY)) minY = pos 
            if (pos.isLeft(minX)) minX = pos 
            if (pos.isRight(maxX)) maxX = pos
        }

        val mapWidth = maxX.subtract(minX).x + 1
        val mapHeight = maxY.subtract(minY).y + 1

        val image = new BufferedImage(util.Constants.TILE_SIZE * mapWidth, util.Constants.TILE_SIZE * mapHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()

        // draw each tile on the grid relative to the top left
        for ((pos, tile) <- this.grid) {
            val relativePos = pos.subtract(Posn(minX.x, maxY.y))
            g2d.drawImage(tile.render, relativePos.x * TILE_SIZE, -1 * relativePos.y * TILE_SIZE, null)
        }
        g2d.dispose()

        ImageIO.write(image, "png", new File("map.png"))
        image
    }
}

object MapMap {

    def fromJMap(jmap: JsValue): MapMap = {
        var grid: MutMap[Posn, Tile] = MutMap[Posn, Tile]()
        jmap match {
            case JsArray(elements) => elements.map(jrow => QSerializer.deserializeJRow(jrow, grid))
            case _ => throw new IllegalArgumentException("JMap was not a JSON array.")
        }
        return new MapMap(grid.toMap)
    }
}
