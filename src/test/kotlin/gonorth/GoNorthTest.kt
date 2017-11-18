package gonorth

import gonorth.domain.*
import gonorth.domain.Move.*
import kategory.Option
import kategory.getOrElse
import kategory.some
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoNorthTest {
    private val goNorth = GoNorth()

    private val gameState = TestConstants.gameState

    @Test
    fun thePlayerStartsAtTheStartingPlace() {
        assertTrue(gameState.gameText.preText == "You venture into a dark dungeon")
        assertTrue(gameState.location()?.description == "Starting to")
    }

    @Test
    fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, NORTH, Option.None)

        assertEquals("You stumble ahead", newState.gameText.preText)
        assertTrue(newState.location()?.description.orEmpty().contains("You went north and died"))
        assertTrue(newState.gameText.description.getOrElse { "" }.contains("You went north and died"))
        assertFalse(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, EAST, Option.None)

        assertEquals("You head east...", newState.gameText.preText)

        assertEquals("and won!", newState.location()?.description.orEmpty())
        assertEquals("and won!", newState.gameText.description.getOrElse { "" })

        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenAMovementAnyCommandsAreIgnored() {
        val newState = goNorth.takeAction(gameState, EAST, Option.None)
        val newStateAfterCommand = goNorth.takeAction(gameState, EAST, "Dance".some())

        assertEquals(newState, newStateAfterCommand)
    }

    @Test
    fun canDescribeATarget() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE,"Key".some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("It's a shiny golden key.", newState.gameText.description.getOrElse{""})
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetThatDoesntExist() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, "Fox".some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("There is no Fox", newState.gameText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetUsingTheHelperMethod() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, "Fox".some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("There is no Fox", newState.gameText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }
}
