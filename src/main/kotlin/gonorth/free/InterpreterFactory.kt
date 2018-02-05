package gonorth.free

import arrow.HK
import arrow.core.FunctionK
import arrow.core.Id
import arrow.core.IdHK
import gonorth.domain.*
import gonorth.world.WorldBuilder

class InterpreterFactory() {
    fun impureGameEffectInterpreter(gameState: GameState): FunctionK<GameEffect.F, IdHK> {
        return object : FunctionK<GameEffect.F, IdHK> {
            // Todo Replace with a state monad? see Cats
            var gs: GameState = gameState.resetGameText()

            override fun <A> invoke(fa: HK<GameEffect.F, A>): Id<A> {
                val op = fa.ev()

                return when (op) {
                    is GameEffect.KillPlayer -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(player = gs.player.copy(alive = false))
                        Id.pure(gs)
                    }
                    is GameEffect.TeleportPlayer -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(currentLocation = op.locationUUID)
                        gs = gs.findLocation(op.locationUUID)
                                .map { it.description }
                                .fold({ gs }, { s -> gs.appendDescription(s) })
                        Id.pure(gs)
                    }
                    is GameEffect.OneWayLink -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.link.from, op.link.to, op.link.move, op.link.description)
                                .world)
                        Id.pure(gs)
                    }
                    is GameEffect.TwoWayLink -> {
                        gs = gs.appendDescription(op.text)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.link.from, op.link.to, op.link.move, op.link.description)
                                .world)
                        gs = gs.copy(world = WorldBuilder(gs.world)
                                .linkLocation(op.returnLink.from, op.returnLink.to, op.returnLink.move, op.returnLink.description)
                                .world)
                        Id.pure(gs)
                    }
                    is GameEffect.Describe -> {
                        gs = gs.appendDescription(op.text)
                        Id.pure(gs)
                    }
                    is GameEffect.IncreaseHunger -> {
                        val newHunger = gs.player.hunger - op.amount
                        gs = gs.copy(player = gs.player.copy(hunger = newHunger, alive = newHunger > 0))

                        Id.pure(gs)
                    }
                    is GameEffect.ReduceHunger -> {
                        gs = gs.copy(player = gs.player.copy(hunger = Math.min(1000, gs.player.hunger + op.amount)))
                        Id.pure(gs)
                    }
                    is GameEffect.Destroy -> {
                        gs = gs.removeUseable(op.itemName)

                        Id.pure(gs)
                    }
                } as Id<A>
            }
        }
    }
}