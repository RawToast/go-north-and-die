package gonorth.slack

import gonorth.*
import gonorth.domain.location
import kategory.Option
import kategory.getOrElse
import kategory.nonEmpty
import org.junit.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class SimpleGameClientTest {

    val goNorth = GoNorth()
    val gameClient: GameClient = SimpleGameClient(emptyMap(), goNorth, SimpleWorldGenerator())

    @Test
    fun createShouldCreateANewGameWhenGivenValidInput() {
        val user = "Dave"
        val gameStateOption: GameState = gameClient.startGame(user)

        assertTrue { gameStateOption.preText.preText.isNotEmpty() }
        assertTrue { gameStateOption.preText.description.nonEmpty() }
    }

    @Test
    fun takeInputShouldMoveThePlayerWhenGivenAValidDirection() {
        val user = "Jones"
        val textOpt = "NORTH"
        val createdGame: GameState = gameClient.startGame(user)
        val resultOpt: Option<GameState> = gameClient.takeInput(user, textOpt)
        val result = resultOpt.getOrElse { createdGame }

        assertTrue { resultOpt.nonEmpty() }
        assertNotEquals(createdGame, result)

        val initialText = createdGame.preText
        val resultText = result.preText

        assertTrue(resultText.preText.isNotEmpty())
        assertNotEquals(initialText.preText, resultText.preText)
        assertNotEquals(initialText.description, resultText.description)
        assertTrue(resultText.description.getOrElse { "" }.isNotEmpty())
        assertNotEquals(resultText.preText, resultText.description.getOrElse { "" })

        assertNotEquals(createdGame.location(), result.location())
    }

}

