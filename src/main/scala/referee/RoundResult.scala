package referee

/*
Represents the knowledge the Referee needs about a round of Q. 
Includes whether there were no placements in the round and who misbehaved.
*/
case class RoundResult(endsGame: Boolean, misbehaved: List[String])