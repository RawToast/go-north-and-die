package gonorth

import gonorth.domain.Location
import gonorth.domain.Move
import gonorth.domain.location
import gonorth.domain.locationOpt
import gonorth.world.WorldBuilder
import kategory.Option
import kategory.getOrElse
import java.util.*

fun main(args: Array<String>) {
    val client = TerminalClient(GoNorth())

    client.startGame()
}

class TerminalClient(val goNorth: GoNorth) {

    fun startGame() {
        val p1 = Location(UUID.randomUUID(), "Starting to")
        val p2 = Location(UUID.randomUUID(), "Other to")
        val p3 = Location(UUID.randomUUID(), "You went north and died")

        val world = WorldBuilder().newLocation(p1)
                .newLocation(p2)
                .newLocation(p3)
                .twoWayLink(p1, p2, Move.EAST, Move.WEST, "You go east", "Back")
                .linkLocation(p1, p3, Move.NORTH, "You stumble ahead")
                .world


        val gameState = GameStatez("For some reason you played this game", world, p1.id)

        val input: () -> String? = { readLine() }
        val output: (String) -> Unit = { it: String -> println(it) }

        output(gameState.preText)
        output(gameState
                .locationOpt()
                .map { it.description }
                .getOrElse { "location not found" })

        game(gameState, input, output)
    }

    fun <T> T?.toOpt(): Option<T> {
        return Option.fromNullable(this)
    }

    private fun game(gs: GameStatez, inp: () -> String?, out: (String) -> Unit): GameStatez {
        return if (gs.world.links[gs.currentLocation]?.isEmpty() == true) gs
        else {
            val i = inp().orEmpty()

            val resGame:GameStatez = Move.values()
                    .find { m -> m.name == i }
                    .toOpt()
                    .fold({ gs.copy(preText = "Invalid input") },
                            { m -> goNorth.takeAction(gs, m) })

            val currentLocation = resGame.world.locations.find { it.id == gs.currentLocation }!!

            out(resGame.preText)
            out(currentLocation.description)
            out("Moves: ${resGame.world.links
                    .getOrDefault(gs.currentLocation, emptySet())
                    .map { it.move.name }}")

            game(resGame, inp, out)
        }
    }

}