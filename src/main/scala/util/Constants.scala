package util

import scala.collection.mutable.ListBuffer
import atomic.Shape.Shape
import atomic.Shape

object Constants {
    val TOTAL_TILES: Int = 1080
    val TILE_SIZE: Int = 100
    val NUM_EACH_TILE: Int = 30
    val NUM_START_TILE = 6
    val Q_BONUS = 8
    val END_BONUS = 4

    val centerX = TILE_SIZE / 2
    val centerY = TILE_SIZE / 2
    val outerRadius = TILE_SIZE / 2
    val innerRadius = TILE_SIZE / 5

    val angleDelta = Math.PI / 4 
    var angle = 0.0
    var x1, y1, x2, y2 = 0.0
    var eightStarXPoints = ListBuffer[Int]()
    var eightStarYPoints = ListBuffer[Int]()

    for (_ <- 0 until 8) {
        eightStarXPoints += centerX + (outerRadius * Math.cos(angle)).toInt
        eightStarYPoints += centerY + (outerRadius * Math.sin(angle)).toInt

        eightStarXPoints += centerX + (innerRadius * Math.cos(angle + angleDelta / 2)).toInt
        eightStarYPoints += centerY + (innerRadius * Math.sin(angle + angleDelta / 2)).toInt

        angle += angleDelta
    }

    val xPointsMap: Map[Shape, Array[Int]] = Map(
        atomic.Shape.STAR -> Array(0, TILE_SIZE / 2, TILE_SIZE, 3 * TILE_SIZE / 4, TILE_SIZE, TILE_SIZE / 2, 0, TILE_SIZE / 4),
        atomic.Shape.EIGHTSTAR -> eightStarXPoints.toArray,
        atomic.Shape.DIAMOND -> Array(0, TILE_SIZE / 2, TILE_SIZE, TILE_SIZE / 2)
    )

    val yPointsMap: Map[Shape, Array[Int]] = Map(
        atomic.Shape.STAR -> Array(0, TILE_SIZE / 4, 0, TILE_SIZE / 2, TILE_SIZE, 3 * TILE_SIZE / 4, TILE_SIZE, TILE_SIZE / 2),
        atomic.Shape.EIGHTSTAR -> eightStarYPoints.toArray,
        atomic.Shape.DIAMOND -> Array(TILE_SIZE / 2, 0, TILE_SIZE / 2, TILE_SIZE)
    )
}
