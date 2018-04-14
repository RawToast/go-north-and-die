package gonorth.domain

import arrow.core.*
import arrow.syntax.collections.tail
import java.util.*

fun GameState.location(): Location? {
    return this.world
            .locations
            .find { (id) -> id == this.currentLocation }
}

fun GameState.locationOpt(): Option<Location> =
        this.location().toOption()

fun GameState.currentDescription(): Option<String> =
        this.locationOpt().map { it.description }

fun GameState.fetchLinks(uuid: UUID): Option<Set<Link>> =
        this.world.links[uuid].toOption()

fun GameState.findLocation(uuid: UUID): Option<Location> =
        this.world.locations.find { it.id == uuid }.toOption()

fun GameState.findItem(target: String): Option<Item> =
        this.locationOpt().flatMap {
            it.items.onlyItems().findItem(target)
        }

fun GameState.findUsable(target: String): Option<Useable> =
        this.locationOpt().flatMap { it.items.findUsable(target) }

fun GameState.findUsableInCurrentLocation(target: String): Option<Useable> =
        this.locationOpt()
                .flatMap { it.items.findUsable(target) }

fun GameState.findUsableInInventory(target: String): Option<Useable> =
        this.player.inventory.findUsable(target)

fun GameState.findPossibleUseable(target: String): Option<Useable> {
    val worldItem = this.findUsableInCurrentLocation(target)
    val item = this.findUsableInInventory(target)

    return item.or(worldItem)
}

fun GameState.removeItem(target: String): GameState =
        this.copy(world = this.world.copy(locations =
        this.world.locations.map {
            it.copy(items = it.items.filterNot {
                when (it) {
                    is Item -> it.name.equals(target, ignoreCase = true)
                    is FixedItem -> false
                }
            }.toSet())
        }.toSet()))

fun GameState.removeUseable(target: String): GameState =
        this.copy(world = this.world.copy(locations =
        this.world.locations.map {
            it.copy(items = it.items.filterNot {
                it.name().equals(target, ignoreCase = true)
            }.toSet())
        }.toSet()))

fun GameState.addToInventory(item: Item): GameState =
        this.copy(player = this.player.copy(inventory = this.player.inventory.plus(item)))

fun GameState.removeFromInventory(name: String): GameState =
        this.copy(player = this.player
                .copy(inventory = this.player.inventory.filterNot { it.name == name }.toSet()))

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
            .map { d -> GameText(this.gameText.preText, Option.just(d)) }


    return newDescr.foldLeft(this, { gs, gt -> gs.copy(gameText = gt) })
}

fun GameState.appendDescription(textToAppend: String): GameState =
        this.copy(gameText = this.gameText.copy(description =
        Option(this.gameText.description.map { desc -> desc + "\r" + textToAppend }
                .getOrElse { textToAppend })))


fun GameState.appendPretext(textToAppend: String): GameState =
        this.copy(gameText =
        this.gameText.copy(preText =
        if (this.gameText.preText.isBlank()) textToAppend
        else this.gameText.preText + "\n" + textToAppend))


fun GameState.resetGameText(): GameState =
        this.copy(gameText = this.gameText.copy(preText = "", description = this.locationOpt().map { it.description }))
                .updateTextWithItems()

fun GameState.updateGameText(preText: String, description: Option<String>): GameState =
        this.copy(gameText = GameText(preText, description))

fun GameState.moveItemToInventory(target: String): Option<GameState> =
        this.findItem(target)
                .map { this.removeItem(it.name).addToInventory(it) }

// Not GameState based. These are candidates for relocation
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

fun RandomEffects.fetchEffect(seed: Long): List<ItemEffect> {
    val totalWeight = this.effects.fold(1, { i, we -> we.weight + i })
    val roll = Random(seed).nextInt(totalWeight)

    tailrec fun getEffects(list: List<WeightedEffect>, acc: Int): List<ItemEffect> {
        return when {
            list.isEmpty() -> emptyList()
            list.size == 1 -> list.single().effects
            list.first().weight >= acc -> list.first().effects
            else -> getEffects(list.tail(), acc - list.first().weight)
        }
    }

    return getEffects(this.effects, roll)
}
