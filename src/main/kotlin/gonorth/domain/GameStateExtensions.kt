package gonorth.domain

import gonorth.toOpt
import kategory.Option
import kategory.getOrElse
import java.util.*

fun GameState.location(): Location? {
    return this.world
            .locations
            .find { (id) -> id == this.currentLocation }
}

fun GameState.locationOpt(): Option<Location> =
        this.location().toOpt()

fun GameState.fetchLinks(uuid: UUID): Option<Set<Link>> =
        this.world.links[uuid].toOpt()

fun GameState.findLocation(uuid: UUID): Option<Location> =
        this.world.locations.find { it.id == uuid }.toOpt()

fun GameState.findItem(target: String): Option<Item> =
        this.locationOpt().flatMap { it.items.find { it.name.equals(target, ignoreCase = true) }.toOpt() }


fun GameState.removeItem(target: String): GameState =
        this.copy(world = this.world.copy(locations =
        this.world.locations.map {
            it.copy(items = it.items.filterNot { it.name.equals(target, ignoreCase = true) }.toSet())
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
            d.replace("{${i.name}}", i.ingameText, ignoreCase = true)
        }
    }
            .map { it.replace(Regex(pattern = """[{]\w*[}]"""), "") }
            .map { d -> GameText(this.gameText.preText, Option.Some(d)) }


    return newDescr.foldL(this, { gs, gt -> gs.copy(gameText = gt) })
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

