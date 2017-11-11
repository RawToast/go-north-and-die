package gonorth

import gonorth.domain.Move
import gonorth.domain.World
import gonorth.domain.locationOpt
import kategory.Option
import kategory.getOrElse
import java.util.*

class GoNorth {

    fun takeAction(gameState: GameState, move: Move): GameState {
        return gameState.locationOpt()
                .flatMap { gameState.world.links[it.id].toOpt() }
                .flatMap { it.find { it.move == move }.toOpt() }
                .map { (id, _, preText) -> gameState.copy(preText = preText, currentLocation = id) }
                .getOrElse { gameState }
    }

    fun <T> T?.toOpt(): Option<T> {
        return Option.fromNullable(this)
    }
}

data class GameState(val preText: String, val world: World, val currentLocation: UUID)

