package gonorth

import arrow.core.None
import gonorth.domain.*
import gonorth.free.GameEffect
import gonorth.free.GameEffect.Describe
import gonorth.free.GameEffect.Destroy
import gonorth.world.WorldBuilder

const val SEED = 1234567890L

object TestConstants {
    val startingLocationUUID = "start"
    val location2UUID = "location2"
    val location3UUID = "location3"
    val location4UUID = "location4"


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