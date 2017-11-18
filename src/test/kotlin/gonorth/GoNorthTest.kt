package gonorth

import gonorth.domain.*
import gonorth.domain.Move.*
import gonorth.world.WorldBuilder
import kategory.Option
import kategory.getOrElse
import kategory.some
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
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
    val player = Player(1000, emptySet(), alive = true)

    val gameState = GameState(gameText, world, location1.id, player, 1234567890L)

    @Test
    fun thePlayerStartsAtTheStartingPlace() {
        assertTrue(gameState.gameText.preText == "You venture into a dark dungeon")
        assertTrue(gameState.location()?.description == "Starting to")
    }

    @Test
    fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, NORTH)

        assertEquals("You stumble ahead", newState.gameText.preText)
        assertTrue(newState.location()?.description.orEmpty().contains("You went north and died"))
        assertTrue(newState.gameText.description.getOrElse { "" }.contains("You went north and died"))
        assertFalse(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, EAST)

        assertEquals("You head east...", newState.gameText.preText)

        assertEquals("and won!", newState.location()?.description.orEmpty())
        assertEquals("and won!", newState.gameText.description.getOrElse { "" })

        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATarget() {
        val testWorld = builder
                .placeItem(location1, Item("Key", "It's a shiny golden key."))
                .world
        val testGameState = gameState.copy(world = testWorld)
        val newState = goNorth.takeActionWithTarget(testGameState, DESCRIBE,"Key")

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("It's a shiny golden key.", newState.gameText.description.getOrElse{""})
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetThatDoesntExist() {
        val testWorld = builder
                .placeItem(location1, Item("Key", "It's a shiny golden key."))
                .world
        val testGameState = gameState.copy(world = testWorld)
        val newState = goNorth.takeActionWithTarget(testGameState, DESCRIBE, "Fox")

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("There is no Fox", newState.gameText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetUsingTheHelperMethod() {
        val testWorld = builder
                .placeItem(location1, Item("Key", "It's a shiny golden key."))
                .world
        val testGameState = gameState.copy(world = testWorld)
        val newState = goNorth.takeAnyAction(testGameState, DESCRIBE, "Fox".some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("There is no Fox", newState.gameText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }
}
