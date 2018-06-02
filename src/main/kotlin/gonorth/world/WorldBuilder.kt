package gonorth.world

import arrow.data.getOption
import arrow.syntax.function.pipe
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

        return world.links.getOption(from.id)
            .map { it.filterNot { l -> l.to == to.id && move == l.move } }
            .map { it.plus(link) }
            .fold({ setOf(link) }, { l -> l.toSet() })
            .pipe { world.links.plus(Pair(from.id, it)) }
            .pipe { WorldBuilder(World(world.locations, it)) }
    }

    fun linkLocation(from: String, to: String, move: Move, description: String): WorldBuilder {

        val link = Link(to, move, description)

        return world.links.getOption(from)
            .map { it.filterNot { l -> l.to == to && move == l.move } }
            .map { it.plus(link) }
            .fold({ setOf(link) }, { l -> l.toSet() })
            .pipe { world.links.plus(Pair(from, it)) }
            .pipe { WorldBuilder(World(world.locations, it)) }
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
            .pipe { WorldBuilder(World(it, world.links)) }
}
