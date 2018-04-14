package gonorth

import arrow.core.None
import arrow.core.Try
import arrow.core.getOrElse
import arrow.core.toOption
import gonorth.domain.*
import gonorth.free.InterpreterFactory
import java.util.*

fun main(args: Array<String>) {

    val interpreter = InterpreterFactory()
    val goNorth = GoNorth(interpreter)
    val gameClient = ConsoleClient(goNorth, SimpleGameStateGenerator(), PossibilityGenerator())

    fun consoleout(str: String) = println(str)
    val input = {
        Try.just(System.console().readPassword().map { it.toLowerCase() }.firstOrNull() ?: ' ').getOrElse { ' ' }
    }

    fun gameLoop(): Boolean {
        consoleout("")
        consoleout("****************************")
        consoleout("Welcome to Go North and Die!")
        consoleout("****************************")
        consoleout("")


        val game = gameClient.startGame(System.currentTimeMillis())

        tailrec fun playGame(gameState: GameState): Boolean =
                if (gameState.player.alive) {
                    consoleout(gameState.gameText.preText)

                    val dsc = gameState.gameText.description.getOrElse { "" }
                    if (dsc.isNotEmpty()) consoleout(dsc)

                    val ns = gameClient.takeInput(input, gameState)
                    playGame(ns)
                } else {
                    consoleout("Game Over")
                    false
                }

        return playGame(game)
    }

    gameLoop()
}


interface SimpleClient {

    fun startGame(seed: Long): GameState

    fun takeInput(awaitInput: () -> Char, gameState: GameState): GameState
}

class ConsoleClient(private val engine: GoNorth,
                    private val worldBuilder: GameStateGenerator,
                    private val parser: PossibilityGenerator) : SimpleClient {

    override fun startGame(seed: Long): GameState {
        val r = Random(seed).nextLong()
        val player = Player(1000, emptySet(), alive = true)


        return worldBuilder.generate(player, r)
    }


    override fun takeInput(awaitInput: () -> Char, currentSTATE: GameState): GameState {
        val consoleOutput: (String) -> Unit = { s -> println(s) }
        fun loggy(stuff: String) {
            Thread.sleep(500)
            consoleOutput(stuff)
        }

        fun rootChoices(ic: InputChoices): List<Pair<Char, String>> {
            val mv = if (ic.movement.isNotEmpty()) listOf('q' to "Move") else emptyList()
            val ds = if (ic.describe.isNotEmpty()) listOf('w' to "Describe") else emptyList()
            val tk = if (ic.take.isNotEmpty()) listOf('e' to "Take") else emptyList()
            val us = if (ic.use.isNotEmpty()) listOf('r' to "Use") else emptyList()

            return mv.plus(ds).plus(tk).plus(us)
        }


        fun <K, A, B> Map<K, A>.foldLeft(b: B, f: (B, Map.Entry<K, A>) -> B): B {
            var result = b
            this.forEach { result = f(result, it) }
            return result
        }

        tailrec fun handleInputs(gameState: GameState, input: () -> Char, output: (String) -> Unit): GameState {
            val inputChoices = parser.generate(gameState)
            val topLevelChoices = rootChoices(inputChoices)

            output("Moves: " + topLevelChoices.joinToString(separator = ", ") { kv -> "${kv.first}:${kv.second}" })

            val c = input()

            return when {
                !topLevelChoices.map { it.first }.contains(c) -> {
                    output("Please select a valid action")
                    handleInputs(gameState, input, output)
                }
                c != 'q' && c != 'w' && c != 'e' && c != 'r' -> handleInputs(gameState, input, output)
                c == 'q' -> {
                    output("Choose a direction")
                    val nextChoice = inputChoices.movement

                    output(inputChoices.movement.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                    val i2 = awaitInput()

                    return if (!nextChoice.containsKey(i2)) {
                        output("Please select a valid action")

                        handleInputs(gameState, input, output)
                    } else {
                        engine.takeAction(gameState, when {
                            (i2 == 'w') -> Move.NORTH
                            (i2 == 'a') -> Move.WEST
                            (i2 == 's') -> Move.SOUTH
                            (i2 == 'd') -> Move.EAST
                            else -> Move.NORTH // Improve this
                        }, None)
                    }
                }
                c == 'w' -> {
                    output("Describe?")
                    val nextChoices = inputChoices.describe

                    output(nextChoices.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                    val i2 = input()

                    return if (!nextChoices.containsKey(i2)) {
                        output("Please select a valid action")

                        handleInputs(gameState, input, output)
                    } else {
                        engine.takeAction(gameState, Move.DESCRIBE, nextChoices[i2].toOption())
                    }
                }
                c == 'e' -> {
                    output("Take?")
                    val nextChoices = inputChoices.take
                    output(nextChoices.foldLeft("", { s, m -> s + m.key + ":" + m.value + " " }))

                    val i2 = input()

                    return if (!nextChoices.containsKey(i2)) {
                        output("Please select a valid action")

                        handleInputs(gameState, input, output)
                    } else {
                        engine.takeAction(gameState, Move.DESCRIBE, nextChoices[i2].toOption())
                    }
                }
                c == 'r' -> {
                    output("Use?")
                    val nextChoices = inputChoices.use
                    val i2 = input()

                    return if (!nextChoices.containsKey(i2)) {
                        output("Please select a valid action")

                        handleInputs(gameState, input, output)
                    } else {
                        engine.takeAction(gameState, Move.DESCRIBE, nextChoices[i2].toOption())
                    }
                }
                else -> {
                    output("Impossible state")
                    gameState
                }
            }
        }

        return handleInputs(currentSTATE, awaitInput, consoleOutput)
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
