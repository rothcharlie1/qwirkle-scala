package referee

/*
The knowledge a Referee must have about a turn, including:
- misbehaved: did the player misbehave?
- placed: did the player make a placing turn?
- placedAll: did the player place all its tiles?
*/
object TurnResult extends Enumeration {
    type TurnResult = Value

    val MISBEHAVED = Value
    val PLACED = Value
    val PLACEDALL = Value
    val NOTPLACED = Value
    
    def endsGame(res: TurnResult) = res match {
        case PLACEDALL => true
        case _ => false
    }
}