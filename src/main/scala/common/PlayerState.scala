package common

import spray.json._
import atomic.PrivatePlayerInfo
import atomic.PublicPlayerInfo

/*
Represents the state of a player in a QGame. 
Provides functions necessary to make and validate turns for this Player.
*/
class PlayerState(var tiles: List[Tile], val age: Int, val id: String, private var score: Int = 0) extends Ordered[PlayerState] {
    if (age < 0) throw new IllegalArgumentException("Player instantiated with age less than 0.")
    if (score < 0) throw new IllegalArgumentException("Player instantiated with score less than 0.")

    /*
    Returns a PrivatePlayerInfo corresponding to this player.
    */
    def getPrivatePlayerInfo: PrivatePlayerInfo = PrivatePlayerInfo(tiles, score, id)

    /*
    Returns a PublicPlayerInfo corresponding to this player.
    */
    def getPublicPlayerInfo: PublicPlayerInfo = PublicPlayerInfo(score)

    def addTurnScore(turnScore: Int): Unit = {
        this.score += turnScore
    }

    def giveTiles(newTiles: List[Tile]): Unit = {
        tiles = tiles ++ newTiles
    }

    def swapTiles(refereeTiles: List[Tile]): List[Tile] = {
        if (tiles.size > refereeTiles.size) {
            throw new IllegalArgumentException("Not enough referee tiles provided.")
        } else if (tiles.size < refereeTiles.size) {
            throw new IllegalArgumentException("Too many referee tiles provided.")
        }
        val tempTiles = this.tiles
        this.tiles = refereeTiles
        tempTiles
    }

    def holdsTile(tile: Tile): Boolean = {
        this.tiles.contains(tile)
    }

    def hasTiles: Boolean = {
        this.tiles.size != 0
    }

    def removeTile(tile: Tile): Unit = {
        this.tiles = this.tiles diff List(tile)
    }

    def copy: PlayerState = new PlayerState(tiles, age, id, score)

    /*
    Provides a comparison between this Player's age and another Player's age.
    Allows Player to implement Ordered[Player] and be easily sorted by age when in a List.
    */
    def compare(that: PlayerState): Int = this.age - that.age

    def getScore: Int = this.score

    def toJPlayer: JsValue = {
        val jscore = JsNumber(score)
        val jname = JsString(id)
        val jtiles = JsArray(tiles.map(_.toJTile))
        JsObject(Map("score" -> jscore, "name" -> jname, "tile*" -> jtiles))
    }
    
    /*
    Hashes this Player based on its id.
    Assumes no two Players have the same id.
    */
    override def hashCode: Int = id.hashCode

    /*
    Two Players are equal if their id's are equal.
    */
    override def equals(obj: Any): Boolean = {
        obj match {
            case that: PlayerState => this.id == that.id
            case _ => false
        }
    }
}