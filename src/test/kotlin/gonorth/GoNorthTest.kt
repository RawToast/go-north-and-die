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

    private val KEY: String = "Key"

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

    @Test // I think this must happen!
    fun whenGivenNorthWithTheRealMapThePlayerDies() {
        val stateWithRealWorld = SimpleGameStateGenerator()
                .generate(TestConstants.player, 123L)
        val newState = goNorth.takeAction(stateWithRealWorld, NORTH, Option.None)

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
    fun whenGivenADirectionThatDoesNotExist() {
        val newState = goNorth.takeAction(gameState, SOUTH, Option.None)
        val newState2 = goNorth.takeAction(gameState, WEST, Option.None)

        assertEquals(gameState.gameText.preText, newState.gameText.preText)
        assertEquals(gameState.gameText.description, newState.gameText.description)

        assertEquals(gameState.gameText.preText, newState2.gameText.preText)
        assertEquals(gameState.gameText.description, newState2.gameText.description)
    }

    @Test
    fun whenGivenAMovementAnyCommandsAreIgnored() {
        val newState = goNorth.takeAction(gameState, EAST, Option.None)
        val newStateAfterCommand = goNorth.takeAction(gameState, EAST, "Dance".some())

        assertEquals(newState, newStateAfterCommand)
    }

    @Test
    fun canDescribeATarget() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, KEY.some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("It's a shiny golden key.", newState.gameText.description.getOrElse{""})
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetIgnoresCase() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, "kEy".some())

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
    fun canTakeAnItemWhichRemovesItFromTheLocationAndAddsItToTheInventory() {
        assertEquals(Option.Some(TestConstants.key), gameState.findItem(KEY))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())

        assertEquals(Option.None, newState.findItem(KEY), "The key is removed")
        assertTrue(newState.player.inventory.contains(TestConstants.key), "The player now has the key")

    }

    @Test
    fun canTakeAnItemIgnoresCase() {
        assertEquals(Option.Some(TestConstants.key), gameState.findItem("keY"))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())

        assertEquals(Option.None, newState.findItem("KeY"), "The key is removed")
        assertTrue(newState.player.inventory.contains(TestConstants.key), "The player now has the key")
    }
}
