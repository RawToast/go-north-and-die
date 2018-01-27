package gonorth.slack

import arrow.core.Option
import arrow.core.getOrElse
import gonorth.ActionInterpreterFactory
import gonorth.GameClient
import gonorth.GoNorth
import gonorth.SimpleGameClient
import gonorth.domain.GameState
import gonorth.domain.Move
import gonorth.domain.TinyGameStateGenerator
import gonorth.domain.location
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class SimpleGameClientTest {
    private val factory = ActionInterpreterFactory()
    val goNorth = GoNorth(factory)
    val gameClient: GameClient = SimpleGameClient(emptyMap(), goNorth, TinyGameStateGenerator())

    @Test
    fun createShouldCreateANewGameWhenGivenValidInput() {
        val user = "Dave"
        val gameStateOption: GameState = gameClient.startGame(user)

        assertTrue { gameStateOption.gameText.preText.isNotEmpty() }
        assertTrue { gameStateOption.gameText.description.nonEmpty() }
    }

    @Test
    fun takeInputShouldMoveThePlayerWhenGivenAValidDirection() {
        val user = "Davey"
        val textOpt = "NORTH"
        val createdGame: GameState = gameClient.startGame(user)
        val resultOpt: Option<GameState> = gameClient.takeInput(user, textOpt)
        val result = resultOpt.getOrElse { createdGame }

        assertTrue { resultOpt.nonEmpty() }
        assertNotEquals(createdGame, result)

        val initialText = createdGame.gameText
        val resultText = result.gameText

        assertTrue(resultText.preText.isNotEmpty())
        assertNotEquals(initialText.preText, resultText.preText)
        assertNotEquals(initialText.description, resultText.description)
        assertTrue(resultText.description.getOrElse { "" }.isNotEmpty())
        assertNotEquals(resultText.preText, resultText.description.getOrElse { "" })

        assertNotEquals(createdGame.location(), result.location())
    }

    @Test
    fun takeInputShouldMoveThePlayerWhenGivenAValidDirectionIgnoringCase() {
        val user = "Davey"
        val textOpt = "NoRtH"
        val createdGame: GameState = gameClient.startGame(user)
        val resultOpt: Option<GameState> = gameClient.takeInput(user, textOpt)
        val result = resultOpt.getOrElse { createdGame }

        assertTrue { resultOpt.nonEmpty() }
        assertNotEquals(createdGame, result)

        val initialText = createdGame.gameText
        val resultText = result.gameText

        assertTrue(resultText.preText.isNotEmpty())
        assertNotEquals(initialText.preText, resultText.preText)
        assertNotEquals(initialText.description, resultText.description)
        assertTrue(resultText.description.getOrElse { "" }.isNotEmpty())
        assertNotEquals(resultText.preText, resultText.description.getOrElse { "" })

        assertNotEquals(createdGame.location(), result.location())
    }

    @Test
    fun takeInputShouldHandleAValidCommandRequest() {
        val user = "Jones"
        val textOpt = "${Move.DESCRIBE.name} Key"
        val createdGame: GameState = gameClient.startGame(user)
        val resultOpt: Option<GameState> = gameClient.takeInput(user, textOpt)
        val result = resultOpt.getOrElse { createdGame }

        assertTrue { resultOpt.nonEmpty() }
        assertNotEquals(createdGame, result)

        val initialText = createdGame.gameText
        val resultText = result.gameText

        assertTrue(resultText.preText.isNotEmpty())
        assertNotEquals(initialText.preText, resultText.preText)
        assertNotEquals(initialText.description, resultText.description)
        assertTrue(resultText.description.getOrElse { "" }.isNotEmpty())
        assertNotEquals(resultText.preText, resultText.description.getOrElse { "" })

        assertEquals(createdGame.location(), result.location())
    }

}

