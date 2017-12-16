package gonorth

import gonorth.domain.GameState
import gonorth.domain.GameStateGenerator
import gonorth.domain.Move
import gonorth.domain.Player
import kategory.Option
import java.util.*

interface GameClient {

    fun startGame(userId: String): GameState

    fun takeInput(userId: String, input: String): Option<GameState>

}


class SimpleGameClient(private var db: Map<String, GameState>,
                       private val engine: GoNorth,
                       private val worldBuilder: GameStateGenerator) : GameClient {

    override fun takeInput(userId: String, input: String): Option<GameState> {
        // Valid requests from Slack are pretty limited:
        // /gnad EAST
        // /gnad DESCRIBE Key
        val moveStr = input.substringBefore(' ')
        val command = input.substringAfter(" ")

        val commandOpt = if (command == moveStr) {
            Option.None
        } else {
            Option.Some(command)
        }


        val result = db[userId].toOpt()
                .flatMap { gs ->
                    Move.values()
                            .find { m -> m.name.equals(moveStr, ignoreCase = true) }
                            .toOpt()
                            .map { engine.takeAction(gs, it, commandOpt)  }
                }

        db = result.fold({ db }, { db.plus(Pair(userId, it)) })

        return result
    }

    override fun startGame(userId: String): GameState {

        val r = Random(System.currentTimeMillis()).nextLong()
        val player = Player(1000, emptySet(), alive = true)

        val gs = worldBuilder.generate(player, r)

        db = db.plus(Pair(userId, gs))
        return gs
    }
}

