package gonorth.world

import gonorth.domain.*
import arrow.data.getOption
import java.util.*

// gonorth.domain.World data structure and building functions

class WorldBuilder(val world: World = World(emptySet(), emptyMap())) {

    fun newLocation(location: Location): WorldBuilder {
        return WorldBuilder(this.world.copy(locations = this.world.locations.plus(location)))
    }

    fun linkLocation(from: Location, to: Location, move: Move, description: String): WorldBuilder {

        val link = Link(to.id, move, description)

        val x: Set<Link> = world.links.getOption(from.id)
                .map { it.filterNot { l -> l.to == to.id && move == l.move } }
                .map { it.plus(link) }
                .fold({ setOf(link) }, { l -> l.toSet() })

        val newLinks = world.links.plus(Pair(from.id, x))

        return WorldBuilder(World(world.locations, newLinks))
    }

    fun linkLocation(from: UUID, to: UUID, move: Move, description: String): WorldBuilder {

        val link = Link(to, move, description)

        val x: Set<Link> = world.links.getOption(from)
                .map { it.filterNot { l -> l.to == to && move == l.move } }
                .map { it.plus(link) }
                .fold({ setOf(link) }, { l -> l.toSet() })

        val newLinks = world.links.plus(Pair(from, x))

        return WorldBuilder(World(world.locations, newLinks))
    }

    fun twoWayLink(from: Location, to: Location, move: Move, returnMove: Move,
                   description: String, returnDescription: String): WorldBuilder {

        return linkLocation(from, to, move, description)
                .linkLocation(to, from, returnMove, returnDescription)
    }

    fun placeItem(at: Location, item: Useable): WorldBuilder {
        val newLocation = world.locations
                .map { if (it.id == at.id) it.copy(items = it.items.plus(item)) else it }
                .toSet()

        return WorldBuilder(World(newLocation, world.links))
    }
}
