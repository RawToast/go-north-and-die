package gonorth.console

import arrow.core.Try
import arrow.core.getOrElse
import org.jline.terminal.Terminal

class GnConsole(private val terminal: Terminal): Console {
    override fun output(text: String) {
        println(text + "\r")
    }

    override fun awaitInput(): Char =
        Try.just(terminal
                .reader()
                .read()
                .toChar()
                .toLowerCase())
                    .getOrElse { ' ' }

    override fun clear() {
        print("\u001B[H\u001B[2J")
    }

    override fun close() {
        terminal.reader().close()
        terminal.close()
    }
}


interface Console {

    fun awaitInput(): Char

    fun output(text: String): Unit

    fun clear(): Unit

    fun close(): Unit
}
