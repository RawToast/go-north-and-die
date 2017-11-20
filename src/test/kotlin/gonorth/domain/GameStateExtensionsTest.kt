package gonorth.domain

import gonorth.TestConstants
import gonorth.domain.location
import gonorth.domain.locationOpt
import gonorth.world.WorldBuilder
import kategory.Option
import kategory.getOrElse
import kategory.nonEmpty
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

        assertTrue(locationOpt.map{ it.id == TestConstants.location2UUID}.getOrElse{false})
        assertTrue(locationOpt2.map{ it.id == TestConstants.location3UUID}.getOrElse{false})
    }

    @Test fun findNothingWhenTheUUIDDoesNotExist() {
        val locationOpt = gameStateWithEmptyWorld.findLocation(TestConstants.location2UUID)
        val locationOpt2 = gameStateWithEmptyWorld.findLocation(TestConstants.location3UUID)

        assertTrue(locationOpt.isEmpty)
        assertTrue(locationOpt2.isEmpty)
    }

    @Test fun canRemoveAnItemFromALocation() {

        val newState = gameState.removeItem("Key")

        assertEquals(Option.Some(TestConstants.key), gameState.findItem("Key"))
        assertEquals(Option.None, newState.findItem("Key"))
    }

    @Test fun canPlaceAnItemInThePlayersInventory() {
        val test = "Test"
        val item = Item(test, test, test)
        val newState:GameState = gameState.addToInventory(item)

        assertNotNull(newState.player.inventory.firstOrNull{it.name == test})
        assertNull(gameState.player.inventory.firstOrNull{it.name == test})
    }

}
