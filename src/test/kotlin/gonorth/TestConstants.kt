package gonorth

import gonorth.domain.*
import gonorth.world.WorldBuilder
import kategory.Option
import java.util.*

const val UUID1: String = "b0529be4-8c14-41d3-9283-f8dfb916a9e1"
const val UUID2 = "4f5a80a5-7456-4777-9b6c-c8e6551f3571"
const val UUID3 = "3aac284d-1bb9-47a2-92ef-23eb9ea26a20"
const val SEED = 1234567890L

object TestConstants {
    val startingLocationUUID = UUID.fromString(UUID1)!!
    val location2UUID = UUID.fromString(UUID2)!!
    val location3UUID = UUID.fromString(UUID3)!!

    val location1 = Location(startingLocationUUID, "Starting to", emptySet())
    val location2 = Location(location2UUID, "You went north and died", emptySet())
    val location3 = Location(location3UUID, "and won!", emptySet())

    private val gameText = GameText("You venture into a dark dungeon",
            Option.None)
    private val player = Player(1000, emptySet(), alive = true)

    private val builder = WorldBuilder().newLocation(location1)
            .newLocation(location2)
            .newLocation(location3)
            .linkLocation(location1, location2, Move.NORTH, "You stumble ahead")
            .twoWayLink(location1, location3,
                    Move.EAST, Move.WEST, "You head east...", "You stroll west")
            .placeItem(location1, Item("Key", "It's a shiny golden key."))

    val world = builder.world

    val gameState = GameState(gameText, world, location1.id, player, SEED)
}