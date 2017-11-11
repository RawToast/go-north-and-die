package gonorth

import gonorth.domain.Move
import gonorth.domain.World
import gonorth.domain.locationOpt
import kategory.Option
import kategory.getOrElse
import java.util.*

class GoNorth {

    fun takeAction(gameStatez: GameStatez, m: Move): GameStatez {
        return gameStatez.locationOpt()
                .flatMap { gameStatez.world.links[it.id].toOpt() }
                .flatMap { it.find { it.move == m }.toOpt() }
                .map { (id, _, preText) -> gameStatez.copy(preText = preText, currentLocation = id) }
                .getOrElse { gameStatez }
    }

    fun <T> T?.toOpt(): Option<T> {
        return Option.fromNullable(this)
    }
}

data class GameStatez(val preText: String, val world: World, val currentLocation: UUID)

