package gonorth.domain

import java.util.*

data class World(val locations: Set<Location>, val links: Map<UUID, Set<Links>>)

data class Location(val id: UUID, val description: String)
data class Links(val to: UUID, val move: Move, val description: String)

enum class Move {
    NORTH,
    EAST,
    SOUTH,
    WEST
}