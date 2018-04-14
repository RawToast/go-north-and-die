package gonorth.free

import arrow.Kind
import arrow.core.ForId
import arrow.core.FunctionK
import arrow.core.Id
import gonorth.domain.*
import gonorth.world.WorldBuilder

class InterpreterFactory() {
    fun impureGameEffectInterpreter(gameState: GameState): FunctionK<GameEffect.F, ForId> {
        return object : FunctionK<GameEffect.F, ForId> {
            // Todo Replace with a state monad? see Cats
            var gs: GameState = gameState.resetGameText()

            override fun <A> invoke(fa: Kind<GameEffect.F, A>): Id<A> {
                val op = fa.ev()

                return when (op) {
                    is GameEffect.KillPlayer -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(player = gs.player.copy(alive = false))
                        Id.just(gs)
                    }
                    is GameEffect.TeleportPlayer -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(currentLocation = op.locationUUID)
                        gs = gs.findLocation(op.locationUUID)
                                .map { it.description }
                                .fold({ gs }, { s -> gs.appendDescription(s) })
                        Id.just(gs)
                    }
                    is GameEffect.OneWayLink -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.link.from, op.link.to, op.link.move, op.link.description)
                                .world)
                        Id.just(gs)
                    }
                    is GameEffect.TwoWayLink -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.link.from, op.link.to, op.link.move, op.link.description)
                                .world)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.returnLink.from, op.returnLink.to, op.returnLink.move, op.returnLink.description)
                                .world)
                        Id.just(gs)
                    }
                    is GameEffect.Describe -> {
                        gs = gs.appendDescription(op.text)
                        Id.just(gs)
                    }
                    is GameEffect.IncreaseHunger -> {
                        val newHunger = gs.player.hunger - op.amount
                        gs = gs.copy(player = gs.player.copy(hunger = newHunger, alive = newHunger > 0))

                        Id.just(gs)
                    }
                    is GameEffect.ReduceHunger -> {
                        gs = gs.copy(player = gs.player.copy(hunger = Math.min(1000, gs.player.hunger + op.amount)))
                        Id.just(gs)
                    }
                    is GameEffect.Destroy -> {
                        gs = gs.removeUseable(op.itemName)

                        Id.just(gs)
                    }
                    is GameEffect.RemoveItem -> {
                        gs = gs.removeFromInventory(op.itemName)

                        Id.just(gs)
                    }
                } as Id<A>
            }
        }
    }
}