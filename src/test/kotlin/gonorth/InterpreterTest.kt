package gonorth

import gonorth.domain.GameState
import kategory.*
import org.junit.Test
import kotlin.test.assertTrue

class ActionInterpreterTest {

    val actionInterpreter = ActionInterpreterFactory()
    val gameState = TestConstants.gameState

    @Test
    fun canIncludeADescription() {
        val interpreter = actionInterpreter.createInterpreter(gameState)
        val effect= GameEffect.describe("Something happens")

        val result = effect.foldMap(interpreter, Id.monad())
                                        .ev().value

        assertTrue { result.gameText.description.exists { it.contains("Something happens") } }
    }

    @Test
    fun canCompleteMultipleActions() {
        val interpreter = actionInterpreter.createInterpreter(gameState)
        val effect1= GameEffect.describe("Something happens")
        val effect2= GameEffect.describe("And then something else!")
        val effect3= GameEffect.describe("And then something moar!!!")

        val result = listOf(effect1, effect2, effect3)
                .reduce { op1, op2 -> op1.flatMap { op2 } }
                .foldMap( interpreter, Id.monad() )
                .ev().value

        fun descriptionIncludes(gs: GameState, text: String) =
                gs.gameText.description.map { it.contains(text) }.getOrElse { false }

        assertTrue { result.gameText.description.nonEmpty() }
        assertTrue { descriptionIncludes( result, "Something happens") }
        assertTrue { descriptionIncludes( result, "\n") }
        assertTrue { descriptionIncludes( result, "And then something else!") }
        assertTrue { descriptionIncludes( result, "And then something moar!!!") }
    }


}