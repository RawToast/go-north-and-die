package gonorth.domain

import gonorth.GameStatez
import kategory.Option

fun GameStatez.location(): Location? {
    return this.world
            .locations
            .find { l -> l.id == this.currentLocation }
}

fun GameStatez.locationOpt(): Option<Location> {
    return Option.fromNullable(this.location())
}