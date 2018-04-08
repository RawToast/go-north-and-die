package gonorth.domain

import arrow.core.None
import arrow.core.Some
import gonorth.free.GameEffect.Describe
import gonorth.free.GameEffect.KillPlayer
import gonorth.free.GameEffect.OneWayLink
import gonorth.free.GameEffect.RemoveItem
import gonorth.free.GameEffect.TeleportPlayer
import gonorth.free.GameEffect.LinkDetails
import gonorth.world.WorldBuilder
import java.util.*

interface GameStateGenerator {
    fun generate(player: Player, seed: Long): GameState
}


/**
 * Generator that builds a simple game world, showcasing basic features of the engine.
 *
 * Ideally world creation should not be hardcoded.
 */
class SimpleGameStateGenerator : GameStateGenerator {
    override fun generate(player: Player, seed: Long): GameState {

        val doorLocationUUID = UUID.randomUUID()
        val towerLocationUUID = UUID.randomUUID()
        val weight25 = 25

        val key = Item("Key", "Shiny key, looks useful",
                " except for a small golden key",
                Some(doorLocationUUID),
                FixedEffects(listOf(
                        Describe("The key gets stuck in the lock as you turn... but the door opens!"),
                        RemoveItem("Key"),
                        OneWayLink(LinkDetails(doorLocationUUID, towerLocationUUID, Move.NORTH,
                                "You open the door walk and enter the tower. The door slams shut behind you!"),
                                "You unlock the tower door"))))

        val axe = Item("Axe", "Sharp looking axe",
                " and a small axe lying next to a pile of firewood",
                Some(doorLocationUUID), RandomEffects(
                listOf(
                        WeightedEffect(75, listOf(
                                Describe("You start hacking away at the wooden door."),
                                Describe("It doesn't take long before the door starts to give way."),
                                KillPlayer("In your eagerness you take one last wild swing at the door " +
                                        "and accidentally take off your own head.")
                        )),
                        WeightedEffect(weight25, listOf(
                                Describe("You start hacking away at the wooden door."),
                                Describe("It doesn't take long before the door starts to give way."),
                                OneWayLink(LinkDetails(doorLocationUUID, towerLocationUUID, Move.NORTH,
                                        "The door gives way and you enter the tower."),
                                        "You enter the tower")))
                )
        ))
        val tower = Location(doorLocationUUID,
                "The path takes you to a huge stone tower with a locked wooden door.", emptySet())

        val button = FixedItem("Button", "You wonder what this button does",
                " A large shiny button is beside the path.",
                RandomEffects(
                        listOf(
                                WeightedEffect(weight25, listOf(
                                        Describe("You press the button."),
                                        Describe("Nothing else seems to happen. That was an anti-climax")
                                )),
                                WeightedEffect(weight25, listOf(
                                        Describe("You press the button."),
                                        KillPlayer("And then you spontaneously implode!")
                                )),
                                WeightedEffect(weight25, listOf(
                                        Describe("After a moment to contemplate you press the button."),
                                        Describe("You feel drowsy... and fall asleep."),
                                        TeleportPlayer(tower.id, "You wake up on a dirt path.")
                                )),
                                WeightedEffect(weight25, listOf(
                                        Describe("After a moment to contemplate you press the button."),
                                        Describe("Nothing seems to happen.")
                                )),
                                WeightedEffect(weight25, listOf(
                                        Describe("You poke the button."),
                                        Describe("You wonder why the developers would put in such a pointless item.")
                                ))
                        )
                ))

        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.", emptySet())
        val p2 = Location(UUID.randomUUID(),
                "You come to a clearing in the forest where the path comes to an abrupt end. Amongst the fallen trees there are many tree stumps{axe}.", setOf(axe))
        val p3 = Location(UUID.randomUUID(), "You went north and died.", emptySet())
        val p4 = Location(UUID.randomUUID(),
                "The road continues to the west, whilst a side path heads south. {button}", setOf(button))
        val p5 = Location(UUID.randomUUID(), "You come to an opening in the forest. " +
                "The path is green with moss{key}. A large river blocks your path.", setOf(key))
        val p6 = Location(UUID.randomUUID(), "To the north you spot a large tower.", emptySet())

        val place7 = tower

        val p8 = Location(towerLocationUUID,
                "You walk inside the tower. Rocks fall, You die.", emptySet())


        val world = WorldBuilder().newLocation(p1)
                .newLocation(p2)
                .newLocation(p3)
                .newLocation(p4)
                .newLocation(p5)
                .newLocation(p6)
                .newLocation(place7)
                .newLocation(p8)
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
                .linkLocation(p6, place7, Move.NORTH, "You head north towards the tower")
                .world

        val startingText = GameText(
                "You find yourself lost in a dark forest.",
                Some("It might be wise to find shelter for the night.")
        )

        return GameState(startingText, world, p1.id, player, seed)
    }
}

class TinyGameStateGenerator : GameStateGenerator {
    override fun generate(player: Player, seed: Long): GameState {
        val key = Item("Key", "Shiny key, looks useful", "A key rests on the ground.",
                None, FixedEffects(listOf(Describe("The key is super shiny!"))))

        val p1 = Location(UUID.randomUUID(), "There is a fork in the path.", setOf(key))
        val p3 = Location(UUID.randomUUID(), "You went north and died.", emptySet())

        val world = WorldBuilder().newLocation(p1)
                .newLocation(p3)
                .linkLocation(p1, p3, Move.NORTH, "You stumble ahead")
                .world

        val startingText = GameText(
                "You find yourself lost in a dark forest.",
                Some("It might be wise to find shelter for the night.")
        )

        return GameState(startingText, world, p1.id, player, seed)
    }
}