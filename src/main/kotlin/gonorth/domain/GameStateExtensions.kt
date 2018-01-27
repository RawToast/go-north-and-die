package gonorth.domain

import arrow.core.Option
import arrow.core.getOrElse
import arrow.syntax.option.toOption
import java.util.*

fun GameState.location(): Location? {
    return this.world
            .locations
            .find { (id) -> id == this.currentLocation }
}

fun GameState.locationOpt(): Option<Location> =
        this.location().toOption()

fun GameState.fetchLinks(uuid: UUID): Option<Set<Link>> =
        this.world.links[uuid].toOption()

fun GameState.findLocation(uuid: UUID): Option<Location> =
        this.world.locations.find { it.id == uuid }.toOption()

fun GameState.findItem(target: String): Option<Item> =
        this.locationOpt().flatMap {
            it.items.filter { i ->
                when (i) {
                    is Item -> true
                    is FixedItem -> false
                }
            }.findUsable(target)
        } as Option<Item>

fun GameState.findUsable(target: String): Option<Useable> =
        this.locationOpt().flatMap { it.items.findUsable(target) }

fun Collection<Useable>.findUsable(target: String): Option<Useable> =
        this.find {
            when (it) {
                is Item -> it.name.equals(target, ignoreCase = true)
                is FixedItem -> false
            }
        }.toOption()

fun GameState.removeItem(target: String): GameState =
        this.copy(world = this.world.copy(locations =
        this.world.locations.map {
            it.copy(items = it.items.filterNot {
                when (it) {
                    is Item -> it.name.equals(target, ignoreCase = true)
                    is FixedItem -> false
                }
            }.toSet())
        }
                .toSet()))

fun GameState.addToInventory(item: Item): GameState =
        this.copy(player = this.player.copy(inventory = this.player.inventory.plus(item)))


// Descriptive

fun GameState.updateTextWithItems(): GameState {
    val items = this.locationOpt()
            .map { it.items }
            .getOrElse { emptySet() }


    val newDescr: Option<GameText> = this.gameText.description.map {
        items.fold(it) { d, i ->
            val pair: Pair<String, String> = when (i) {
                is Item -> Pair(i.name, i.ingameText)
                is FixedItem -> Pair(i.name, i.ingameText)
            }

            d.replace("{${pair.first}}", pair.second, ignoreCase = true)
        }
    }
            .map { it.replace(Regex(pattern = """[{]\w*[}]"""), "") }
            .map { d -> GameText(this.gameText.preText, Option.pure(d)) }


    return newDescr.foldLeft(this, { gs, gt -> gs.copy(gameText = gt) })
}

fun GameState.appendDescription(textToAppend: String): GameState {
    return this.copy(gameText = this.gameText.copy(description =
    Option(this.gameText.description.map { desc -> desc + "\n" + textToAppend }
            .getOrElse { textToAppend })))
}

fun GameState.appendPretext(textToAppend: String): GameState {
    return this.copy(gameText = this.gameText.copy(preText =
    if (this.gameText.preText.isBlank()) textToAppend
    else this.gameText.preText + "\n" + textToAppend))
}

fun GameState.resetGameText(): GameState {
    return this.copy(gameText = this.gameText.copy(preText = "", description = this.locationOpt().map { it.description }))
            .updateTextWithItems()
}

