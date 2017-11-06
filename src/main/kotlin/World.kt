// World data structure and building functions

// Basic struct
data class Place(val description: String, val linkedPlaces: Set<Place>, val moves: Map<Move, String>)

// Basic example
object World {

    fun addNewPlace(description: String, from: Place, how: Map<Move, String>): Place {
        val np = Place(description, setOf(from), mapOf())
        return from.copy(
                linkedPlaces = from.linkedPlaces.plus(np),
                moves = from.moves.plus(how))
    }

}