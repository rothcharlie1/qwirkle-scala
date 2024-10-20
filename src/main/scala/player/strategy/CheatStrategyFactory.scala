package player.strategy

object CheatStrategyFactory {

    def create(jcheat: String, base: Strategy): Strategy = jcheat match {
        case "non-adjacent-coordinate" => new NonAdjacentCheatStrategy(base)
        case "tile-not-owned" => new TileNotOwnedCheatStrategy(base)
        case "not-a-line" => new NotALineCheatStrategy(base)
        case "bad-ask-for-tiles" => new BadAskForTilesCheatStrategy(base)
        case "no-fit" => new NoFitCheatStrategy(base)
    }

}