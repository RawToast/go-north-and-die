package gonorth

import com.fasterxml.jackson.databind.JsonSerializer
import gonorth.domain.Item
import gonorth.domain.Location
import gonorth.domain.Move
import gonorth.domain.location
import gonorth.slack.toOpt
import gonorth.world.WorldBuilder
import kategory.Option
import kategory.getOrElse
import java.util.*

fun main(args: Array<String>) {
    val client = TerminalClient(GoNorth())

    client.startGame()
}

interface GameClient {

    fun startGame(userId: String): GameState

    fun takeInput(userId: String, input: String): Option<GameState>

}

class SpikeGameClient(var db: Map<String, GameState>, val engine: GoNorth) : GameClient {
    override fun takeInput(userId: String, input: String): Option<GameState> {

        val moveOpt = input.substringAfter(' ')
        val command = input.substringBefore(" ")


        val commandOpt = if (command == input) {
            Option.None
        } else {
            Option.Some(command)
        }

        val res = db[userId].toOpt()
                .flatMap { gs ->
                    Move.values()
                            .find { m -> m.name == input }
                            .toOpt()
                            .map { engine.takeAnyAction(gs, it, commandOpt)  }
                }

        db = res.fold( {db}, { db.plus(Pair(userId, it)) })

        return res
    }

    override fun startGame(userId: String): GameState {
        val gs = BasicWorld.generate()

        db = db.plus(Pair(userId, gs))
        return gs
    }
}

/**
 * Basic client that runs in the terminal. This is just an experimental client for basic manual testing.
 *
 * @param goNorth GoNorth game logic
 */
class TerminalClient(val goNorth: GoNorth) {

    fun startGame() {
        val gameState = BasicWorld.generate()

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
                    .fold({ gs.copy(preText = GameText("Invalid input",
                                    Option.Some("It's not possible to go in that direction"))) },
                            { m -> goNorth.takeAction(gs, m) })

            outputToTerminal(resGame, output)

            game(resGame, input, output)
        }
    }

    private fun outputToTerminal(gameState: GameState, out: (String) -> Unit) {
        val currentLocation = gameState.location()!!
        val moves = gameState.world.links
                .getOrDefault(gameState.currentLocation, emptySet())
                .map { it.move.name }

        out(gameState.preText.preText)
        if(!gameState.preText.description.isEmpty) {
            out(gameState.preText.description.getOrElse { "" })
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

object BasicWorld {
    fun generate(): GameState {
        val key = Item("Key", "Shiny key, looks useful")

        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.", emptySet())
        val p2 = Location(UUID.randomUUID(), "The path comes to an abrupt end.", emptySet())
        val p3 = Location(UUID.randomUUID(), "You went north and died.", emptySet())
        val p4 = Location(UUID.randomUUID(),
                "The road continues to the west, whilst a side path heads south.", emptySet())
        val p5 = Location(UUID.randomUUID(), "A river blocks your path. A key rests on the ground. ", setOf(key))
        val p6 = Location(UUID.randomUUID(), "To the north you spot a large tower.", emptySet())
        val p7 = Location(UUID.randomUUID(),
                "You look at the tower door in front of you. Rocks fall, You die.", emptySet())



        val world = WorldBuilder().newLocation(p1)
                .newLocation(p2)
                .newLocation(p3)
                .newLocation(p4)
                .newLocation(p5)
                .newLocation(p6)
                .newLocation(p7)
                .twoWayLink(p1, p2, Move.EAST, Move.WEST,
                        "You take the path heading east",
                        "You make your way back to the crossroads")
                .twoWayLink(p1, p4, Move.WEST, Move.EAST,
                        "You take the path heading west",
                        "You make your way back to the crossroads")
                .twoWayLink(p4, p5, Move.SOUTH, Move.NORTH,
                        "You take the beaten path south",
                        "You take the worn path north")
                .twoWayLink(p4, p6, Move.WEST, Move.EAST,
                        "You continue along the western trail",
                        "As you head west, you ponder whether println can print strings")
                .linkLocation(p1, p3, Move.NORTH, "You stumble ahead")
                .linkLocation(p6, p7, Move.NORTH, "You head north towards the tower")
                .world

        val startingText = GameText(
                "You find yourself lost in a dark forest.",
                Option.Some("It might be wise to find shelter for the night.")
        )

        return GameState(startingText, world, p1.id)
    }
}