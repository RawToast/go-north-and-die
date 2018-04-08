package gonorth.domain

import arrow.core.Option
import arrow.core.Tuple2
import gonorth.free.GameEffect
import java.util.*

data class GameState(val gameText: GameText, val world: World, val currentLocation: UUID,
                     val player: Player, val seed: Long)

data class GameText(val preText: String, val description: Option<String>)

data class World(val locations: Set<Location>, val links: Map<UUID, Set<Link>>)
data class Location(val id: UUID, val description: String, val items: Set<Useable>)
data class Link(val to: UUID, val move: Move, val description: String)

sealed class Useable
typealias ItemEffect = GameEffect<GameState>

data class Item(val name: String, val description: String, val ingameText: String,
                val requiredLocation: Option<UUID>, val effects: Effects): Useable()
data class FixedItem(val name: String, val description: String, val ingameText: String,
                     val effects: Effects): Useable()


sealed class Effects
data class FixedEffects(val effects: List<ItemEffect>): Effects()
data class RandomEffects(val effects: List<WeightedEffect>): Effects()
data class WeightedEffect(val weight: Int, val effects: List<ItemEffect>)


data class Player(val hunger: Int, val inventory: Set<Item>, val alive:Boolean)

enum class Move {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    DESCRIBE,
    TAKE,
    USE,
    EAT
}

