package gonorth

import gonorth.domain.*
import kategory.*

class GoNorth {

    fun takeAction(gameState: GameState, move: Move, command: Option<String>): GameState =
            handleActionWithTarget(move, gameState, command.getOrElse { "" })

    private fun handleActionWithTarget(move: Move, gameState: GameState, target: String): GameState = when(move) {
        Move.NORTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.EAST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.SOUTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.WEST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.DESCRIBE -> describe(gameState, target)
        Move.TAKE -> take(gameState, target)
        Move.USE -> use(gameState, target)
        Move.EAT -> eat(gameState, target)
    }

    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
        val location: Option<Link> = gameState.locationOpt()
                .flatMap { gameState.fetchLinks(it.id) }
                .flatMap { it.find { it.move == move }.toOpt() }

        val newPlace = location.flatMap { l ->
            gameState.findLocation(l.to) }
                .map { it.description }

        return location
                .map { (id, _, preText) -> gameState.copy(
                        gameText = GameText(preText, newPlace),
                        currentLocation = id) }
    }

    private fun describe(gameState: GameState, target: String): GameState {
        val item = gameState.findItem(target)
                            .map { it.description }
                            .getOrElse { "There is no $target" }
                            .some()

        return gameState.copy(gameText = GameText("You take a closer look.", item))
    }

    private fun take(gameState: GameState, target: String): GameState {

        val gsWithItem = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }
                .getOrElse { gameState }

        val text = GameText( "You take the $target" , Option.Some(""))

        return gsWithItem.copy(gameText = text)
    }

    private fun use(gameState: GameState, target: String): GameState {
        return gameState
    }

    private fun eat(gameState: GameState, target: String): GameState {
        return gameState
    }

}
