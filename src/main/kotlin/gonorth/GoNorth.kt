package gonorth

import gonorth.domain.*
import kategory.*
import kategory.Option.*
import java.util.*

class GoNorth {

    fun takeAnyAction(gameState: GameState, move: Move, command: Option<String>): GameState {
        return if (movementActions.contains(move) && command.isEmpty) {
            takeAction(gameState, move)
        } else if (movementActions.contains(move) && command.nonEmpty()) {
            takeActionWithTarget(gameState, move, command.getOrElse { "" })
        } else gameState
    }

    fun takeAction(gameState: GameState, move: Move): GameState {
        return if (movementActions.contains(move)) {
            handleMovement(gameState, move).getOrElse { gameState }
        } else gameState
    }

    fun takeActionWithTarget(gameState: GameState, move: Move, target: String): GameState =
            handleActionWithTarget(move, gameState, target)

    private fun handleActionWithTarget(move: Move, gameState: GameState, target: String): GameState {
        return if (movementActions.contains(move)) {
            handleMovement(gameState, move).getOrElse { gameState }
        } else {

            val item = gameState.locationOpt()
                    .flatMap {
                        it.items.find { it.name == target }.toOpt()
                                .map { it.description }
                    }

            when (item) {
                is Some -> gameState
                        .copy(preText = GameText("You take a closer look.", item))
                is None -> gameState
                        .copy(preText = GameText("You take a closer look.", Some("There is no $target")))
            }
        }
    }

    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
        val location = gameState.locationOpt()
                .flatMap { gameState.world.links[it.id].toOpt() }
                .flatMap { it.find { it.move == move }.toOpt() }

        val newPlace = location.flatMap { l ->
            gameState.world.locations.find { it.id == l.to}.toOpt() }
                .map { it.description }

        return location
                .map { (id, _, preText) -> gameState.copy(
                        preText = GameText(preText, newPlace),
                        currentLocation = id) }
    }

    private fun <T> T?.toOpt(): Option<T> = Option.fromNullable(this)

    private val movementActions = listOf(Move.NORTH, Move.SOUTH, Move.EAST, Move.WEST)
    private val targetActions = listOf(Move.DESCRIBE)
}

data class GameState(val preText: GameText, val world: World, val currentLocation: UUID)

data class GameText(val preText: String, val description: Option<String>)

