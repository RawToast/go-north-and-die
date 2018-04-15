package gonorth.console

import arrow.core.None
import arrow.core.toOption
import gonorth.GoNorth
import gonorth.domain.GameState
import gonorth.domain.GameStateGenerator
import gonorth.domain.INITIAL_HUNGER
import gonorth.domain.Move
import gonorth.domain.Player
import java.util.Random


interface SimpleClient {

    fun startGame(seed: Long): GameState

    fun takeInput(gameState: GameState): GameState
}

class ConsoleClient(private val engine: GoNorth,
                    private val console: Console,
                    private val worldBuilder: GameStateGenerator,
                    private val parser: PossibilityFilter) : SimpleClient {

    override fun startGame(seed: Long): GameState {
        val r = Random(seed).nextLong()
        val player = Player(INITIAL_HUNGER, emptySet(), alive = true)


        return worldBuilder.generate(player, r)
    }


    override fun takeInput(gameState: GameState): GameState {

        fun rootChoices(ic: InputChoices): List<Pair<Char, String>> {
            val moves = if (ic.movement.isNotEmpty()) listOf('q' to "Move") else emptyList()
            val describe = if (ic.describe.isNotEmpty()) listOf('w' to "Describe") else emptyList()
            val take = if (ic.take.isNotEmpty()) listOf('e' to "Take") else emptyList()
            val use = if (ic.use.isNotEmpty()) listOf('r' to "Use") else emptyList()

            return moves.plus(describe).plus(take).plus(use)
        }

        fun handleInputs(gameState: GameState, console: Console): GameState {
            val inputChoices = parser.filter(gameState)
            val topLevelChoices = rootChoices(inputChoices)

            if (topLevelChoices.isNotEmpty())
                console.output("Moves: " + topLevelChoices.joinToString(separator = ", ")
                { kv -> "${kv.first}:${kv.second}" })

            fun doAction(choices: Choices, console: Console, action: Move, prefix: String = ""): GameState {
                console.output(prefix + choices.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                val input = console.awaitInput()

                return if (choices.containsKey(input)) engine.takeAction(gameState, action, choices[input].toOption())
                else handleInputs(gameState, console)
            }

            val inputChar = console.awaitInput()

            return when {
                !topLevelChoices.map { it.first }.contains(inputChar) -> {
                    handleInputs(gameState, console)
                }
                topLevelChoices.isEmpty() ->
                    gameState.copy(player = gameState.player.copy(alive = false))
                inputChar == 'q' -> {
                    val choices = inputChoices.movement
                    console.output("Choose a direction: " +
                            inputChoices.movement.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                    val input = console.awaitInput()

                    return if (!choices.containsKey(input)) handleInputs(gameState, console)
                    else {
                        engine.takeAction(gameState, when {
                            input == 'w' -> Move.NORTH
                            input == 'a' -> Move.WEST
                            input == 's' -> Move.SOUTH
                            input == 'd' -> Move.EAST
                            else -> Move.NORTH // Improve this
                        }, None)
                    }
                }
                inputChar == 'w' -> return doAction(inputChoices.describe, console, Move.DESCRIBE, "Describe? ")
                inputChar == 'e' -> return doAction(inputChoices.take, console, Move.TAKE, "Take? ")
                inputChar == 'r' -> return doAction(inputChoices.use, console, Move.USE, "Use? ")
                else -> {
                    console.output("Impossible state")
                    gameState
                }
            }
        }

        return handleInputs(gameState, console)
    }

    private fun <K, A, B> Map<K, A>.foldLeft(b: B, f: (B, Map.Entry<K, A>) -> B): B {
        var result = b
        this.forEach { result = f(result, it) }
        return result
    }
}

typealias Choices = Map<Char, String>

data class InputChoices(val movement: Choices, val describe: Choices, val take: Choices, val use: Choices)
