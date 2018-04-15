package gonorth

import arrow.core.getOrElse
import gonorth.console.ConsoleClient
import gonorth.console.GnConsole
import gonorth.console.PossibilityFilter
import gonorth.domain.GameState
import gonorth.domain.SimpleGameStateGenerator
import gonorth.free.InterpreterFactory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

fun main(args: Array<String>) {

    val terminal: Terminal = TerminalBuilder.builder()
            .jna(true)
            .system(true)
            .build()
    terminal.enterRawMode()

    val console = GnConsole(terminal)

    val interpreter = InterpreterFactory()
    val goNorth = GoNorth(interpreter)
    val gameClient = ConsoleClient(goNorth, console, SimpleGameStateGenerator(), PossibilityFilter())

    fun gameLoop(): Boolean {

        console.clear()
        console.output("")
        console.output("****************************")
        console.output(" Go North and Die!")
        console.output("****************************")
        console.output("")
        console.output("")

        val game = gameClient.startGame(System.currentTimeMillis())

        tailrec fun playGame(gameState: GameState, doClear: Boolean = true): Boolean =
                if (gameState.player.alive) {
                    if (doClear) console.clear()
                    console.output(gameState.gameText.preText)

                    val dsc = gameState.gameText.description.getOrElse { "" }
                    if (dsc.isNotEmpty()) console.output(dsc)

                    val ns = gameClient.takeInput(gameState)
                    playGame(ns)
                } else {
                    console.output("Game Over")
                    false
                }

        return playGame(game, doClear = false)
    }

    gameLoop()

    console.close()
    System.exit(0)
}
