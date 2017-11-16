package gonorth

import gonorth.domain.Item
import gonorth.domain.Move
import gonorth.domain.World
import gonorth.domain.locationOpt
import kategory.Option
import kategory.getOrElse
import java.util.*

class GoNorth {

    fun takeAction(gameState: GameState, move: Move): GameState {
        return if (movementActions.contains(move)) {
            handleMovement(gameState, move).getOrElse { gameState }
        } else gameState
    }

    fun takeActionWithTarget(gameState: GameState, move: Move, target: String): GameState {
        return if (movementActions.contains(move)) {
            handleMovement(gameState, move).getOrElse { gameState }
        } else {

            gameState.locationOpt()
                    .map { it.items.filter { it.name == target } }
                    .getOrElse { emptyList() }
                    .singleOrNull { it.name == target }
                    .toOpt()
                    .map { i -> gameState.copy(preText = "You take a closer look.",  = i.description) }
                    .getOrElse { gameState.copy(preText = "You take a closer look.",  = "There is no $target") }
        }
    }

    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
        return gameState.locationOpt()
                .flatMap { gameState.world.links[it.id].toOpt() }
                .flatMap { it.find { it.move == move }.toOpt() }
                .map { (id, _, preText) -> gameState.copy(preText = preText, currentLocation = id) }
    }

    private fun <T> T?.toOpt(): Option<T> = Option.fromNullable(this)

    private val movementActions = listOf(Move.NORTH, Move.SOUTH, Move.EAST, Move.WEST)
    private val targetActions = listOf(Move.DESCRIBE)
}

data class GameState(val preText: String, val world: World, val currentLocation: UUID)

