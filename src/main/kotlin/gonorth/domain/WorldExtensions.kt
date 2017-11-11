package gonorth.domain

import gonorth.GameState
import kategory.Option

fun GameState.location(): Location? {
    return this.world
            .locations
            .find { (id) -> id == this.currentLocation }
}

fun GameState.locationOpt(): Option<Location> {
    return Option.fromNullable(this.location())
}