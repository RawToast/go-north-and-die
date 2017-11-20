package gonorth

import gonorth.domain.*
import kategory.Option
import kategory.getOrElse
import java.util.*

interface GameClient {

    fun startGame(userId: String): GameState

    fun takeInput(userId: String, input: String): Option<GameState>

}


class SimpleGameClient(var db: Map<String, GameState>, val engine: GoNorth,
                       val worldBuilder: GameStateGenerator) : GameClient {

    override fun takeInput(userId: String, input: String): Option<GameState> {
        // /gnad EAST
        // /gnad DESCRIBE Key
        val moveStr = input.substringBefore(' ')
        val command = input.substringAfter(" ")

        val commandOpt = if (command == moveStr) {
            Option.None
        } else {
            Option.Some(command)
        }


        val res = db[userId].toOpt()
                .flatMap { gs ->
                    Move.values()
                            .find { m -> m.name == moveStr }
                            .toOpt()
                            .map { engine.takeAction(gs, it, commandOpt)  }
                }

        db = res.fold( {db}, { db.plus(Pair(userId, it)) })

        return res
    }

    override fun startGame(userId: String): GameState {

        val r = Random(System.currentTimeMillis()).nextLong()
        val player = Player(1000, emptySet(), alive = true)

        val gs = worldBuilder.generate(player, r)

        db = db.plus(Pair(userId, gs))
        return gs
    }
}

