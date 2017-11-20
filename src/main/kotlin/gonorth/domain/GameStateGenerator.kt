package gonorth.domain

import gonorth.world.WorldBuilder
import kategory.Option
import java.util.*

interface GameStateGenerator {
    fun generate(player: Player, seed: Long): GameState
}

class SimpleGameStateGenerator : GameStateGenerator {
    override fun generate(player: Player, seed:Long): GameState {
        val key = Item("Key", "Shiny key, looks useful", "A key rests on the ground.")

        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.", emptySet())
        val p2 = Location(UUID.randomUUID(), "The path comes to an abrupt end.", emptySet())
        val p3 = Location(UUID.randomUUID(), "You went north and died.", emptySet())
        val p4 = Location(UUID.randomUUID(),
                "The road continues to the west, whilst a side path heads south.", emptySet())
        val p5 = Location(UUID.randomUUID(), "A river blocks your path.", setOf(key))
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

        return GameState(startingText, world, p1.id, player, seed)
    }
}

class TinyGameStateGenerator : GameStateGenerator {
    override fun generate(player: Player, seed: Long): GameState {
        val key = Item("Key", "Shiny key, looks useful", "A key rests on the ground.")

        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.", setOf(key))
        val p3 = Location(UUID.randomUUID(), "You went north and died.", emptySet())


        val world = WorldBuilder().newLocation(p1)
                .newLocation(p3)
                .linkLocation(p1, p3, Move.NORTH, "You stumble ahead")
                .world

        val startingText = GameText(
                "You find yourself lost in a dark forest.",
                Option.Some("It might be wise to find shelter for the night.")
        )

        return GameState(startingText, world, p1.id, player, seed)
    }
}