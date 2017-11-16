package gonorth

import gonorth.domain.Item
import gonorth.domain.Move.*
import gonorth.world.WorldBuilder

import gonorth.domain.Location
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import gonorth.domain.location
import kategory.Option
import kategory.getOrElse
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoNorthTest {
    val goNorth = GoNorth()

    val location1 = Location(UUID.randomUUID(), "Starting to", emptySet())
    val location2 = Location(UUID.randomUUID(), "You went north and died", emptySet())
    val location3 = Location(UUID.randomUUID(), "and won!", emptySet())

    val builder = WorldBuilder().newLocation(location1)
            .newLocation(location2)
            .newLocation(location3)
            .linkLocation(location1, location2, NORTH, "You stumble ahead")
            .twoWayLink(location1, location3,
                    EAST, WEST, "You head east...", "You stroll west")

    val world = builder.world

    val gameText = GameText("You venture into a dark dungeon",
                    Option.None)
    val gameState = GameState(gameText, world, location1.id)

    @Test
    fun thePlayerStartsAtTheStartingPlace() {
        assertTrue(gameState.preText.preText == "You venture into a dark dungeon")
        assertTrue(gameState.location()?.description == "Starting to")
    }

    @Test
    fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, NORTH)

        assertEquals("You stumble ahead", newState.preText.preText)
        assertTrue(newState.location()?.description.orEmpty().contains("You went north and died"))
        assertTrue(newState.preText.description.getOrElse { "" }.contains("You went north and died"))
        assertFalse(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, EAST)

        assertEquals("You head east...", newState.preText.preText)

        assertEquals("and won!", newState.location()?.description.orEmpty())
        assertEquals("and won!", newState.preText.description.getOrElse { "" })

        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATarget() {
        val testWorld = builder
                .placeItem(location1, Item("Key", "It's a shiny golden key."))
                .world
        val testGameState = gameState.copy(world = testWorld)
        val newState = goNorth.takeActionWithTarget(testGameState, DESCRIBE,"Key")

        assertEquals("You take a closer look.", newState.preText.preText)

        assertEquals("It's a shiny golden key.", newState.preText.description.getOrElse{""})
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetThatDoesntExist() {
        val testWorld = builder
                .placeItem(location1, Item("Key", "It's a shiny golden key."))
                .world
        val testGameState = gameState.copy(world = testWorld)
        val newState = goNorth.takeActionWithTarget(testGameState, DESCRIBE, "Fox")

        assertEquals("You take a closer look.", newState.preText.preText)

        assertEquals("There is no Fox", newState.preText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }
}
