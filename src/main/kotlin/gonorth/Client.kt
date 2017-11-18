package gonorth

import gonorth.domain.*
import gonorth.slack.toOpt
import kategory.Option
import kategory.getOrElse
import java.util.*

fun main(args: Array<String>) {
    val client = TerminalClient(GoNorth(), SimpleGameStateGenerator())

    client.startGame()
}

interface GameClient {

    fun startGame(userId: String): GameState

    fun takeInput(userId: String, input: String): Option<GameState>

}


class SimpleGameClient(var db: Map<String, GameState>, val engine: GoNorth, val worldBuilder: GameStateGenerator) : GameClient {
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

/**
 * Basic client that runs in the terminal. This is just an experimental client for basic manual testing.
 *
 * @param goNorth GoNorth game logic
 */
class TerminalClient(val goNorth: GoNorth, val worldBuilder: GameStateGenerator) {

    fun startGame() {
        val player = Player(1000, emptySet(), alive = true)

        val r = Random(System.currentTimeMillis()).nextLong()

        val gameState = worldBuilder.generate(player, r)

        val input: () -> String? = { readLine() }
        val output: (String) -> Unit = { it: String -> println(it) }

        outputToTerminal(gameState, output)

        game(gameState, input, output)
    }


    private fun game(gs: GameState, input: () -> String?, output: (String) -> Unit): GameState {
        return if (gs.world.links[gs.currentLocation].orEmpty().isEmpty()) gs
        else {
            val i = input().orEmpty()

            val resGame: GameState = Move.values()
                    .find { m -> m.name == i }
                    .toOpt()
                    .fold({ gs.copy(gameText = GameText("Invalid input",
                            Option.Some("It's not possible to go in that direction"))) },
                            { m -> goNorth.takeAction(gs, m, Option.None) })

            outputToTerminal(resGame, output)

            game(resGame, input, output)
        }
    }

    private fun outputToTerminal(gameState: GameState, out: (String) -> Unit) {
        val currentLocation = gameState.location()!!
        val moves = gameState.world.links
                .getOrDefault(gameState.currentLocation, emptySet())
                .map { it.move.name }

        out(gameState.gameText.preText)
        if(!gameState.gameText.description.isEmpty) {
            out(gameState.gameText.description.getOrElse { "" })
        }
        out(currentLocation.description)
        if (moves.isNotEmpty()) {
            out("Moves: $moves")
        }
    }

    private fun <T> T?.toOpt(): Option<T> {
        return Option.fromNullable(this)
    }
}

