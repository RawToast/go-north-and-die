package gonorth.console

import arrow.core.Either
import arrow.core.getOrElse
import org.jline.terminal.Terminal

class GnConsole(private val terminal: Terminal) : Console {
    override fun output(text: String) =
        println(text + "\r")

    override fun awaitInput(): Char =
        Either.catch {
            terminal.reader()
                    .read()
                    .toChar()
                    .lowercaseChar()
        }.getOrElse { ' ' }

    override fun clear() =
        print("\u001B[H\u001B[2J")

    override fun close() {
        terminal.reader().close()
        terminal.close()
    }
}


interface Console {

    fun awaitInput(): Char

    fun output(text: String)

    fun clear()

    fun close()
}
