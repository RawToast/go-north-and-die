package gonorth.console

import gonorth.TestConstants
import org.junit.Test
import kotlin.test.assertTrue


class PossibilityFilterTest {

    private val filter = PossibilityFilter()
    private val gameState = TestConstants.gameState

    @Test
    fun returnsActionsWhenPossible() {
        val result = filter.filter(gameState)
        assertTrue(result.movement.isNotEmpty())
        assertTrue(result.describe.isNotEmpty())
        assertTrue(result.use.isNotEmpty())
        assertTrue(result.take.isNotEmpty())
    }

    @Test
    fun filtersActionsWhenNotPossible() {
        val result = filter.filter(gameState.copy(currentLocation = TestConstants.location2UUID))
        assertTrue(result.movement.isEmpty())
        assertTrue(result.describe.isEmpty())
        assertTrue(result.use.isEmpty())
        assertTrue(result.take.isEmpty())
    }
}
