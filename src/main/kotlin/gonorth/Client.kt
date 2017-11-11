package gonorth

import gonorth.domain.Location
import gonorth.domain.Move
import gonorth.domain.location
import gonorth.world.WorldBuilder
import kategory.Option
import java.util.*

fun main(args: Array<String>) {
    val client = TerminalClient(GoNorth())

    client.startGame()
}

/**
 * Basic client that runs in the terminal. This is just an experimental client for basic manual testing.
 *
 * @param goNorth GoNorth game logic
 */
class TerminalClient(val goNorth: GoNorth) {

    fun startGame() {
        val gameState = generateWorld()

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
                    .fold({ gs.copy(preText = "Invalid input") },
                            { m -> goNorth.takeAction(gs, m) })

            outputToTerminal(resGame, output)

            game(resGame, input, output)
        }
    }

    private fun generateWorld(): GameState {
        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.")
        val p2 = Location(UUID.randomUUID(), "The path comes to an abrupt end.")
        val p3 = Location(UUID.randomUUID(), "You went north and died.")

        val world = WorldBuilder().newLocation(p1)
                .newLocation(p2)
                .newLocation(p3)
                .twoWayLink(p1, p2, Move.EAST, Move.WEST,
                        "You take the path heading east",
                        "You make your way back to the crossroads")
                .linkLocation(p1, p3, Move.NORTH, "You stumble ahead")
                .world


        return GameState("You find yourself lost in a dark forest.", world, p1.id)
    }


    private fun outputToTerminal(gameState: GameState, out: (String) -> Unit) {
        val currentLocation = gameState.location()!!
        val moves = gameState.world.links
                .getOrDefault(gameState.currentLocation, emptySet())
                .map { it.move.name }

        out(gameState.preText)
        out(currentLocation.description)
        if (moves.isNotEmpty()) {
            out("Moves: $moves")
        }
    }

    private fun <T> T?.toOpt(): Option<T> {
        return Option.fromNullable(this)
    }
}