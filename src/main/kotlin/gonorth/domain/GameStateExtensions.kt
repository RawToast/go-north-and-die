package gonorth.domain

import gonorth.toOpt
import kategory.Option
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
        this.world.locations.find { it.id == uuid}.toOpt()

fun GameState.findItem(target: String): Option<Item> =
        this.locationOpt().flatMap { it.items.find { it.name.equals(target, ignoreCase = true) }.toOpt()}


fun GameState.removeItem(target: String): GameState =
        this.copy(world = this.world.copy(locations =
            this.world.locations.map {
                it.copy(items = it.items.filterNot { it.name.equals(target, ignoreCase = true) }.toSet())}
                    .toSet()))

fun GameState.addToInventory(item: Item): GameState =
    this.copy(player = this.player.copy(inventory = this.player.inventory.plus(item)))
