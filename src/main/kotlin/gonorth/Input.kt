package gonorth

import arrow.core.getOrElse
import gonorth.domain.*

class PossibilityGenerator() {

    fun generate(gameState: GameState): InputChoices {

        val emptySelections = emptyMap<Char, String>()

        val movement = gameState
                .fetchLinks(gameState.currentLocation)
                .getOrElse { emptySet() }
                .fold(emptySelections, { m, l ->
                    when (l.move) {
                        Move.NORTH -> m.plus(Pair('w', "North"))
                        Move.EAST -> m.plus(Pair('a', "East"))
                        Move.SOUTH -> m.plus(Pair('s', "South"))
                        Move.WEST -> m.plus(Pair('d', "West"))
                        else -> m.plus(Pair('x', "???"))
                    }
                })

        val describe = gameState.locationOpt()
                .map { it.items }
                .getOrElse { emptySet() }
                .toSortedSet(java.util.Comparator { o1, o2 -> o1.name().compareTo(o2.name()) })
                .fold(emptySelections, { m ,l ->
                    m.plus(((1 + m.size).toChar()) to l.name())
                })

        val take = gameState.locationOpt()
                .map { it.items.onlyItems() }
                .getOrElse { emptyList() }
                .fold(emptySelections, { m, l ->
                    m.plus(((1 + m.size).toChar()) to l.name())
                })


        val fixedUse = gameState.locationOpt()
                .map { it.items.onlyFixed() }
                .getOrElse { emptyList() }
                .fold(emptySelections, { m, l ->
                    m.plus(((1 + m.size).toChar()) to l.name())
                })

        val use = gameState.player.inventory
                .filter { it.requiredLocation.map { it == gameState.currentLocation }.getOrElse { true }}
                .fold(fixedUse, { m, l ->
                    m.plus(((1 + m.size).toChar()) to l.name())
                })

        return InputChoices(movement, describe, take, use)
    }
}

data class InputChoices(val movement: Choices, val describe: Choices, val take: Choices, val use: Choices)

typealias Choices = Map<Char, String>
