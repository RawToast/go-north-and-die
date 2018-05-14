package gonorth.domain

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption

fun Collection<Useable>.findUsable(target: String): Option<Useable> =
        this.find {
            when (it) {
                is Item -> it.name.equals(target, ignoreCase = true)
                is FixedItem -> it.name.equals(target, ignoreCase = true)
            }
        }.toOption()

fun Collection<Useable>.findItem(target: String): Option<Item> =
        this.findUsable(target)
                .flatMap {
                    when (it) {
                        is Item -> Some(it)
                        is FixedItem -> None
                    }
                }

fun Collection<Useable>.onlyItems() =
        this.filter { it is Item }

fun Collection<Useable>.onlyFixed() =
        this.filter { it is FixedItem }

fun Useable.name(): String = when (this) {
    is Item -> this.name
    is FixedItem -> this.name
}

fun Useable.effects(): Effects = when (this) {
    is Item -> this.effects
    is FixedItem -> this.effects
}
