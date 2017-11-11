package gonorth.world

import gonorth.domain.Move
import gonorth.domain.Links
import gonorth.domain.Location
import gonorth.domain.World
import kategory.getOption

// gonorth.domain.World data structure and building functions

// Basic struct
data class Place(val description: String, val links: Set<Link>)

data class Link(val place: Place, val move: Move, val description: String)


class WorldBuilder(val world: World = World(emptySet(), emptyMap())) {

    fun newLocation(location: Location): WorldBuilder {
        return WorldBuilder(this.world.copy(locations = this.world.locations.plus(location)))
    }

    fun linkLocation(from: Location, to: Location, move: Move, description: String): WorldBuilder {

        val link = Links(to.id, move, description)

        val x: Set<Links> = world.links.getOption(from.id)
                .map { it.filterNot { l -> l.to == to.id && move == l.move } }
                .map { it.plus(link) }
                .fold({ setOf(link) }, { l -> l.toSet() })

        val newLinks = world.links.plus(Pair(from.id, x))

        return WorldBuilder(World(world.locations, newLinks))
    }

    fun twoWayLink(from: Location, to: Location, move: Move, returnMove: Move,
                   description: String, returnDescription: String): WorldBuilder {

        return linkLocation(from, to, move, description)
                .linkLocation(to, from, returnMove, returnDescription)
    }
}
