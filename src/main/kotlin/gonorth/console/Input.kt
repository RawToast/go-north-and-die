package gonorth.console

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.toOption
import gonorth.GoNorth
import gonorth.domain.*
import java.util.*


interface SimpleClient {

    fun startGame(seed: Long): GameState

    fun takeInput(gameState: GameState): GameState
}

class ConsoleClient(private val engine: GoNorth,
                    private val console: Console,
                    private val worldBuilder: GameStateGenerator,
                    private val parser: PossibilityGenerator) : SimpleClient {

    override fun startGame(seed: Long): GameState {
        val r = Random(seed).nextLong()
        val player = Player(1000, emptySet(), alive = true)


        return worldBuilder.generate(player, r)
    }


    override fun takeInput(gameState: GameState): GameState {

        fun rootChoices(ic: InputChoices): List<Pair<Char, String>> {
            val mv = if (ic.movement.isNotEmpty()) listOf('q' to "Move") else emptyList()
            val ds = if (ic.describe.isNotEmpty()) listOf('w' to "Describe") else emptyList()
            val tk = if (ic.take.isNotEmpty()) listOf('e' to "Take") else emptyList()
            val us = if (ic.use.isNotEmpty()) listOf('r' to "Use") else emptyList()

            return mv.plus(ds).plus(tk).plus(us)
        }

        fun handleInputs(gameState: GameState, console: Console): GameState {
            val inputChoices = parser.generate(gameState)
            val topLevelChoices = rootChoices(inputChoices)

            if (topLevelChoices.isNotEmpty())
                console.output("Moves: " + topLevelChoices.joinToString(separator = ", ")
                    { kv -> "${kv.first}:${kv.second}" })

            fun doAction(choices: Choices, console: Console, action: Move, prefix:String=""): GameState {
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
                    console.output("Choose a direction: " + inputChoices.movement.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                    val input = console.awaitInput()

                    return if (!choices.containsKey(input)) handleInputs(gameState, console)
                    else {
                        engine.takeAction(gameState, when {
                            (input == 'w') -> Move.NORTH
                            (input == 'a') -> Move.WEST
                            (input == 's') -> Move.SOUTH
                            (input == 'd') -> Move.EAST
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

    fun <K, A, B> Map<K, A>.foldLeft(b: B, f: (B, Map.Entry<K, A>) -> B): B {
        var result = b
        this.forEach { result = f(result, it) }
        return result
    }
}

typealias Choices = Map<Char, String>

data class InputChoices(val movement: Choices, val describe: Choices, val take: Choices, val use: Choices)

class PossibilityGenerator {

    fun generate(gameState: GameState): InputChoices {

        val emptySelections = emptyMap<Char, String>()

        val movement = gameState
                .fetchLinks(gameState.currentLocation)
                .getOrElse { emptySet() }
                .fold(emptySelections, { m, l ->
                    when (l.move) {
                        Move.NORTH -> m.plus(Pair('w', "North"))
                        Move.EAST -> m.plus(Pair('d', "East"))
                        Move.SOUTH -> m.plus(Pair('s', "South"))
                        Move.WEST -> m.plus(Pair('a', "West"))
                        else -> m.plus(Pair('x', "???"))
                    }
                })

        val describe = gameState.locationOpt()
                .map { it.items }
                .getOrElse { emptySet() }
                .toSortedSet(java.util.Comparator { o1, o2 -> o1.name().compareTo(o2.name()) })
                .fold(emptySelections, { m, l ->
                    m.plus(((1 + m.size).toString().first()) to l.name())
                })

        val take = gameState.locationOpt()
                .map { it.items.onlyItems() }
                .getOrElse { emptyList() }
                .fold(emptySelections, { m, l ->
                    m.plus(((1 + m.size).toString().first()) to l.name())
                })


        val fixedUse = gameState.locationOpt()
                .map { it.items.onlyFixed() }
                .getOrElse { emptyList() }
                .fold(emptySelections, { m, l ->
                    m.plus(((1 + m.size).toString().first()) to l.name())
                })

        val use = gameState.player.inventory
                .filter { it.requiredLocation.map { it == gameState.currentLocation }.getOrElse { true } }
                .fold(fixedUse, { m, l ->
                    m.plus(((1 + m.size).toString().first()) to l.name())
                })

        return InputChoices(movement, describe, take, use)
    }
}
