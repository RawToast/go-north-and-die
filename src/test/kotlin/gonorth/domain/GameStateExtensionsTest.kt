package gonorth.domain

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import gonorth.TestConstants
import gonorth.world.WorldBuilder
import org.junit.Test
import kotlin.test.*
import arrow.core.*
class GameStateExtensionsTest {

    private val gameState = TestConstants.gameState
    private val gameStateWithEmptyWorld = gameState.copy(world = WorldBuilder().world)


    @Test
    fun findLocationReturnsTheCurrentLocation() {
        val location = gameState.location()

        assertNotNull(location)
    }

    @Test
    fun findLocationOptReturnsTheCurrentLocationAsAnOption() {
        val location = gameState.locationOpt()

        assertTrue(location.nonEmpty())
    }

    @Test
    fun findLocationReturnsANullableIfInANonExistingLocation() {
        val location = gameStateWithEmptyWorld.location()

        assertNull(location)
    }

    @Test
    fun fetchLinksForAGivenValidUUID() {
        val links = gameState.fetchLinks(TestConstants.startingLocationUUID)

        assertTrue(links.nonEmpty())
        val linksValue = links.getOrElse { emptySet() }

        assertEquals(2, linksValue.size)
    }

    @Test
    fun findALocationWhenGivenAnExistingUUID() {
        val locationOpt = gameState.findLocation(TestConstants.location2UUID)
        val locationOpt2 = gameState.findLocation(TestConstants.location3UUID)

        assertTrue(locationOpt.nonEmpty())
        assertTrue(locationOpt2.nonEmpty())

        assertTrue(locationOpt.map { it.id == TestConstants.location2UUID }.getOrElse { false })
        assertTrue(locationOpt2.map { it.id == TestConstants.location3UUID }.getOrElse { false })
    }

    @Test
    fun findNothingWhenTheUUIDDoesNotExist() {
        val locationOpt = gameStateWithEmptyWorld.findLocation(TestConstants.location2UUID)
        val locationOpt2 = gameStateWithEmptyWorld.findLocation(TestConstants.location3UUID)

        assertTrue(locationOpt.isEmpty())
        assertTrue(locationOpt2.isEmpty())
    }

    @Test
    fun canRemoveAnItemFromALocation() {

        val newState = gameState.removeItem("Key")

        assertEquals(Some(TestConstants.key), gameState.findItem("Key"))
        assertEquals(None, newState.findItem("Key"))
    }

    @Test
    fun canPlaceAnItemInThePlayersInventory() {
        val test = "Test"
        val item = Item(test, test, test, None, FixedEffects(emptyList()))
        val newState: GameState = gameState.addToInventory(item)

        assertNotNull(newState.player.inventory.firstOrNull { it.name == test })
        assertNull(gameState.player.inventory.firstOrNull { it.name == test })
    }

    @Test
    fun canIncludeItemsWithinTheDescription() {
        val gameStateWithDescription = gameState.copy(
                gameText = GameText("Pre is irrelevant",
                        description = Some(TestConstants.location1.description)))

        val newState: GameState = gameStateWithDescription.updateTextWithItems()

        assertNotNull(newState.gameText.preText)
        assertTrue(gameStateWithDescription.gameText.description.nonEmpty(), "Should have description")
        assertTrue(newState.gameText.description.nonEmpty(), "Should have description")

        val preText = newState.gameText.preText
        val description = newState.gameText.description.getOrElse { "" }
        assertFalse(preText.contains("A shiny key is on the floor."))
        assertTrue(description.contains("A shiny key is on the floor."))
    }

    @Test
    fun willRemoveAnyUnmatchedItems() {
        val gameStateWithDescription = gameState.copy(
                gameText = GameText("Pre is irrelevant",
                        description = Some("There is a table apples, {fork}and some shoes")))


        val newState: GameState = gameStateWithDescription.updateTextWithItems()

        assertNotNull(newState.gameText.preText)
        assertTrue(gameStateWithDescription.gameText.description.nonEmpty(), "Should have description")
        assertTrue(newState.gameText.description.nonEmpty(), "Should have description")

        val oldDescription = gameStateWithDescription.gameText.description.getOrElse { "" }
        val newDescription = newState.gameText.description.getOrElse { "" }

        assertEquals("There is a table apples, {fork}and some shoes", oldDescription)
        assertEquals("There is a table apples, and some shoes", newDescription)
    }

    @Test
    fun canAppendTextToTheDescription() {
        val textToAppend = "Text to append"
        val gameStateWithDescription = gameState.copy(
                gameText = GameText("Pre is irrelevant",
                        description = Some(TestConstants.location1.description)))
        val newState = gameStateWithDescription.appendDescription(textToAppend)

        assertTrue(newState.gameText.description.map { it.contains(textToAppend) }.getOrElse { false })
        assertTrue(newState.gameText.description.map { it.contains("\n") }.getOrElse { false })
        assertTrue(newState.gameText.description.map {
            it.contains(TestConstants.location1.description)
        }.getOrElse { false })

    }

    @Test
    fun canAppendTextWhenTheDescriptionDoesNotExist() {
        val textToAppend = "Text to append"

        val newState = gameState.appendDescription(textToAppend)

        assertTrue(newState.gameText.description.map { it.contains(textToAppend) }.getOrElse { false })
        assertFalse(newState.gameText.description.map { it.contains("\n") }.getOrElse { true })
    }

}
