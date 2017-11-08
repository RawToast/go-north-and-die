// World data structure and building functions

// Basic struct
data class Place(val description: String, val links: Set<Link>)

data class Link(val place: Place, val move: Move, val description: String)


object World {
    fun addNewPlace(description: String, from: Place, move: Move, journey: String): Place {

        val np = Place(description, emptySet())

        val link = Link(np, move, journey)

        return from.copy(links = from.links.plus(link))
    }

    fun Place.linkNewPlace(move: Move, journey: String, description: String): Place {
        val np = Place(description, emptySet())

        val link = Link(np, move, journey)

        return this.copy(links = this.links.plus(link))
    }

    fun Place.linkPlace(place: Place, move: Move, journey: String): Place {

        val link = Link(place, move, journey)

        return this.copy(links = this.links.plus(link))
    }
}
