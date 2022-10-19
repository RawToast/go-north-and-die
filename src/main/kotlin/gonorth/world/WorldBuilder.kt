package gonorth.world

import arrow.core.*
import arrow.core.Option
import arrow.core.Option.*
import arrow.*
import gonorth.domain.Link
import gonorth.domain.Location
import gonorth.domain.Move
import gonorth.domain.Useable
import gonorth.domain.World

// gonorth.domain.World data structure and building functions

class WorldBuilder(val world: World = World(emptySet(), emptyMap())) {

    fun newLocation(location: Location): WorldBuilder =
            WorldBuilder(this.world.copy(locations = this.world.locations.plus(location)))

    fun linkLocation(from: Location, to: Location, move: Move, description: String): WorldBuilder {

        val link = Link(to.id, move, description)

        return world.links[from.id]
            .toOption()
            .map { it.filterNot { l -> l.to == to.id && move == l.move } }
            .map { it.plus(link) }
            .fold({ setOf(link) }, { l -> l.toSet() })
            .let { world.links.plus(Pair(from.id, it)) }
            .let { WorldBuilder(World(world.locations, it)) }
    }

    fun linkLocation(from: String, to: String, move: Move, description: String): WorldBuilder {

        val link = Link(to, move, description)

        return world.links.getOrNone(from)
            .map { it.filterNot { l -> l.to == to && move == l.move } }
            .map { it.plus(link) }
            .fold({ setOf(link) }, { l -> l.toSet() })
            .let { world.links.plus(Pair(from, it)) }
            .let { WorldBuilder(World(world.locations, it)) }
    }

    fun twoWayLink(
        from: Location, to: Location, move: Move, returnMove: Move,
        description: String, returnDescription: String
    ): WorldBuilder =
        linkLocation(from, to, move, description)
            .linkLocation(to, from, returnMove, returnDescription)

    fun placeItem(at: Location, item: Useable): WorldBuilder =
        world.locations
            .map { if (it.id == at.id) it.copy(items = it.items.plus(item)) else it }
            .toSet()
            .let { WorldBuilder(World(it, world.links)) }
}
