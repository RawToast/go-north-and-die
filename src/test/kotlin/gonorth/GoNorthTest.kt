package gonorth

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import arrow.syntax.option.some
import gonorth.domain.Move.*
import gonorth.domain.SimpleGameStateGenerator
import gonorth.domain.findItem
import gonorth.domain.location
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoNorthTest {

    private val KEY: String = "Key"

    private val factory = ActionInterpreterFactory()
    private val goNorth = GoNorth(factory)

    private val gameState = TestConstants.gameState

    @Test
    fun thePlayerStartsAtTheStartingPlace() {
        assertTrue(gameState.gameText.preText == "You venture into a dark dungeon")
        assertFalse(gameState.location()?.description.isNullOrBlank())
    }

    @Test
    fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, NORTH, None)

        assertEquals("You stumble ahead", newState.gameText.preText)
        assertTrue(newState.location()?.description.orEmpty().contains("You went north and died"))
        assertTrue(newState.gameText.description.getOrElse { "" }.contains("You went north and died"))
        assertFalse(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test // I think this must happen!
    fun whenGivenNorthWithTheRealMapThePlayerDies() {
        val stateWithRealWorld = SimpleGameStateGenerator()
                .generate(TestConstants.player, 123L)
        val newState = goNorth.takeAction(stateWithRealWorld, NORTH, None)

        assertEquals("You stumble ahead", newState.gameText.preText)
        assertTrue(newState.location()?.description.orEmpty().contains("You went north and died"))
        assertTrue(newState.gameText.description.getOrElse { "" }.contains("You went north and died"))
        assertFalse(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, EAST, None)

        assertEquals("You head east...", newState.gameText.preText)

        assertEquals("and won!", newState.location()?.description.orEmpty())
        assertEquals("and won!", newState.gameText.description.getOrElse { "" })

        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun whenGivenADirectionThatDoesNotExist() {
        val newState = goNorth.takeAction(gameState, SOUTH, None)
        val newState2 = goNorth.takeAction(gameState, WEST, None)

        assertEquals(gameState.gameText.preText, newState.gameText.preText)
        assertEquals(gameState.gameText.description, newState.gameText.description)

        assertEquals(gameState.gameText.preText, newState2.gameText.preText)
        assertEquals(gameState.gameText.description, newState2.gameText.description)
    }

    @Test
    fun whenGivenAMovementAnyCommandsAreIgnored() {
        val newState = goNorth.takeAction(gameState, EAST, None)
        val newStateAfterCommand = goNorth.takeAction(gameState, EAST, "Dance".some())

        assertEquals(newState, newStateAfterCommand)
    }

    @Test
    fun canDescribeATarget() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, KEY.some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("It's a shiny golden key.", newState.gameText.description.getOrElse { "" })
        assertTrue(newState.world.links.containsKey(newState.location()?.id))
    }

    @Test
    fun canDescribeATargetIgnoresCase() {
        val newState = goNorth.takeAction(TestConstants.gameState, DESCRIBE, "kEy".some())

        assertEquals("You take a closer look.", newState.gameText.preText)

        assertEquals("It's a shiny golden key.", newState.gameText.description.getOrElse { "" })
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
        assertEquals(Some(TestConstants.key), gameState.findItem(KEY))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())
        val newDescription = newState.gameText.description.getOrElse { "" }

        assertEquals(None, newState.findItem(KEY), "The key is removed")
        assertTrue(newState.player.inventory.contains(TestConstants.key), "The player now has the key")
        assertTrue(newState.gameText.description.nonEmpty())
        assertFalse(newDescription.contains("key", ignoreCase = true))
        assertEquals("You seem to be in a test. You spot some null pointers to the west. " +
                "An alternative path heads to the east", newDescription)
    }

    @Test
    fun canTakeAnItemIgnoresCase() {
        assertEquals(Some(TestConstants.key), gameState.findItem("keY"))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())

        assertEquals(None, newState.findItem("KeY"), "The key is removed")
        assertTrue(newState.player.inventory.contains(TestConstants.key), "The player now has the key")
    }

    @Test
    fun cantTakeAnItemThatDoesNotExit() {
        assertEquals(None, gameState.findItem("Cat"))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, "Cat".some())

        assertFalse(newState.player.inventory.map { it.name }.contains("Cat"), "The player now has the key")
        assertTrue(newState.gameText.preText.contains("There is no Cat"))
    }


    @Test
    fun canUseAnItem() {
        assertEquals(Some(TestConstants.key), gameState.findItem("keY"))

        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())
        val resultState = goNorth.use(newState, "kEy")

        assertFalse(resultState.gameText == newState.gameText)
    }

    @Test
    fun cannotUseAnItemThePlayerDoesNotHave() {
        val newState = goNorth.takeAction(TestConstants.gameState, TAKE, KEY.some())
        val resultState = goNorth.use(newState, "turnips")

        assertTrue(resultState.gameText != newState.gameText)
        assertTrue(resultState.gameText.description.nonEmpty())
        assertTrue(resultState.gameText.preText.contains("You do not have"))
    }
}
