package gonorth.console;

import arrow.core.getOrElse
import gonorth.domain.*


class PossibilityFilter {

    fun filter(gameState: GameState): InputChoices {

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
