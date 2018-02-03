package gonorth.free

import arrow.core.Id
import arrow.core.ev
import arrow.core.getOrElse
import arrow.core.monad
import arrow.free.flatMap
import arrow.free.foldMap
import gonorth.TestConstants
import gonorth.domain.GameState
import gonorth.domain.Move
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionInterpreterTest {

    val actionInterpreter = InterpreterFactory()
    val gameState = TestConstants.gameState

    @Test
    fun canIncludeADescription() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect = GameEffect.describe("Something happens")

        val result = effect.foldMap(interpreter, Id.monad())
                .ev().value

        assertTrue { result.gameText.description.exists { it.contains("Something happens") } }
    }

    @Test
    fun canCompleteMultipleActions() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect1 = GameEffect.describe("Something happens")
        val effect2 = GameEffect.describe("And then something else!")
        val effect3 = GameEffect.describe("And then something moar!!!")

        val result = listOf(effect1, effect2, effect3)
                .reduce { op1, op2 -> op1.flatMap { op2 } }
                .foldMap(interpreter, Id.monad())
                .ev().value

        fun descriptionIncludes(gs: GameState, text: String) =
                gs.gameText.description.map { it.contains(text) }.getOrElse { false }

        assertTrue { result.gameText.description.nonEmpty() }
        assertTrue { descriptionIncludes(result, "Something happens") }
        assertTrue { descriptionIncludes(result, "\n") }
        assertTrue { descriptionIncludes(result, "And then something else!") }
        assertTrue { descriptionIncludes(result, "And then something moar!!!") }
    }

    @Test
    fun canChangeThePlayersLocation() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect = GameEffect.teleportPlayer(TestConstants.location2UUID, "You are teleported")

        val result = effect.foldMap(interpreter, Id.monad())
                .ev().value

        assertTrue { result.gameText.description.exists { it.contains("You are teleported") } }
        assertEquals(TestConstants.location2UUID, result.currentLocation)
        assertTrue { result.gameText.description.exists { it.contains(TestConstants.location2.description) } }
    }

    @Test
    fun canCreateALinkToAnotherLocation() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect = GameEffect.createOneWayLink(
                GameEffect.LinkDetails(TestConstants.startingLocationUUID, TestConstants.location4UUID,
                        Move.SOUTH, "You walk through the portal"), "A portal appears to the south")

        val result = effect.foldMap(interpreter, Id.monad())
                .ev().value

        assertTrue { result.gameText.description.exists { it.contains("A portal appears to the south") } }
        assertEquals(TestConstants.startingLocationUUID, result.currentLocation)
        assertEquals(2, gameState.world.links[TestConstants.startingLocationUUID]?.size)
        assertEquals(3, result.world.links[TestConstants.startingLocationUUID]?.size)

    }

    @Test
    fun canCreateATwoWayLinkToAnotherLocation() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect = GameEffect.createTwoWayLink(
                GameEffect.LinkDetails(TestConstants.startingLocationUUID, TestConstants.location4UUID,
                        Move.SOUTH, "You walk through the portal"),
                GameEffect.LinkDetails(TestConstants.location4UUID, TestConstants.startingLocationUUID,
                        Move.EAST, "You go back through the portal"),
                "A portal appears to the south")

        val result = effect.foldMap(interpreter, Id.monad())
                .ev().value

        assertTrue { result.gameText.description.exists { it.contains("A portal appears to the south") } }
        assertEquals(TestConstants.startingLocationUUID, result.currentLocation)
        assertEquals(2, gameState.world.links[TestConstants.startingLocationUUID]?.size)
        assertEquals(3, result.world.links[TestConstants.startingLocationUUID]?.size)

        // Links the other location
        assertEquals(1, result.world.links[TestConstants.location4UUID]?.size)
    }

    @Test
    fun canKillThePlayer() {
        val interpreter = actionInterpreter.impureGameEffectInterpreter(gameState)
        val effect = GameEffect.killThePlayer("You explode")

        val result = effect.foldMap(interpreter, Id.monad())
                .ev().value

        assertTrue { result.gameText.description.exists { it.contains("You explode") } }
        assertFalse { result.player.alive }
    }
}