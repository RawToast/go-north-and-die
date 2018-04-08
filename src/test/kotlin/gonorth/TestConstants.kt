package gonorth

import arrow.core.None
import gonorth.domain.*
import gonorth.free.GameEffect
import gonorth.free.GameEffect.Describe
import gonorth.free.GameEffect.Destroy
import gonorth.world.WorldBuilder
import java.util.*

const val UUID1: String = "b0529be4-8c14-41d3-9283-f8dfb916a9e1"
const val UUID2 = "4f5a80a5-7456-4777-9b6c-c8e6551f3571"
const val UUID3 = "3aac284d-1bb9-47a2-92ef-23eb9ea26a20"
const val UUID4 = "9d4c57fd-070d-4b0c-90cc-a25d8647d9e0"

const val SEED = 1234567890L

object TestConstants {
    val startingLocationUUID = UUID.fromString(UUID1)!!
    val location2UUID = UUID.fromString(UUID2)!!
    val location3UUID = UUID.fromString(UUID3)!!
    val location4UUID = UUID.fromString(UUID4)!!


    val key = Item("Key", "It's a shiny golden key.", "A shiny key is on the floor. ",
            None, FixedEffects (listOf(Describe("You try eating the key, it was not tasty."))))

    val button = FixedItem("Button", "You wonder what this button does",
            " A large shiny button is beside the path.",
            RandomEffects(
                    listOf(
                            WeightedEffect(25, listOf(
                                    Describe("You press the button."),
                                    Describe("Nothing else seems to happen. That was an anti-climax")
                            )),
                            WeightedEffect(25, listOf(
                                    Describe("You press the button."),
                                    GameEffect.KillPlayer("And then you spontaneously implode!")
                            )),
                            WeightedEffect(25, listOf(
                                    Describe("The button magically disappears before your eyes!"),
                                    Destroy("Button")
                            )),
                            WeightedEffect(25, listOf(
                                    Describe("After a moment to contemplate you press the button."),
                                    Describe("Nothing seems to happen.")
                            )),
                            WeightedEffect(25, listOf(
                                    Describe("You poke the button."),
                                    Describe("You wonder why the developers would put in such a pointless item.")
                            ))
                    )
            ))

    val location1 = Location(startingLocationUUID,
            "You seem to be in a test. You spot some null pointers to the west. " +
                    "A large stone is nearby.{button}" +
                    "{key}" +
                    "An alternative path heads to the east", setOf(key))
    val location2 = Location(location2UUID, "You went north and died", emptySet())
    val location3 = Location(location3UUID, "and won!", emptySet())
    val location4 = Location(location4UUID, "Secret Location", emptySet())

    private val gameText = GameText("You venture into a dark dungeon",
            None)
    val player = Player(900, emptySet(), alive = true)

    private val builder = WorldBuilder().newLocation(location1)
            .newLocation(location2)
            .newLocation(location3)
            .newLocation(location4)
            .linkLocation(location1, location2, Move.NORTH, "You stumble ahead")
            .twoWayLink(location1, location3,
                    Move.EAST, Move.WEST, "You head east...", "You stroll west")
            .placeItem(location1, button)

    val world = builder.world

    val gameState = GameState(gameText, world, location1.id, player, SEED)
}