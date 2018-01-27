package gonorth

import arrow.HK
import arrow.core.*
import arrow.free.Free
import arrow.free.flatMap
import arrow.free.foldMap
import arrow.free.instances.FreeMonadInstance
import arrow.syntax.option.some
import arrow.syntax.option.toOption
import gonorth.domain.*
import gonorth.world.WorldBuilder
import java.util.*

class GoNorth(private val interpreterFactory: ActionInterpreterFactory) {

    fun takeAction(gameState: GameState, move: Move, command: Option<String>): GameState =
            handleActionWithTarget(move, gameState, command.getOrElse { "" })

    private fun handleActionWithTarget(move: Move, gameState: GameState, target: String): GameState = when (move) {
        Move.NORTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.EAST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.SOUTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.WEST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.DESCRIBE -> describe(gameState, target)
        Move.TAKE -> take(gameState, target)
        Move.USE -> use(gameState, target)
        Move.EAT -> eat(gameState, target)
    }

    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
        val linkToNewLocation: Option<Link> = gameState.locationOpt()
                .flatMap { gameState.fetchLinks(it.id) }
                .flatMap { it.find { it.move == move }.toOption() }

        val newPlace = linkToNewLocation.flatMap { l ->
            gameState.findLocation(l.to)
        }
                .map { it.description }

        return linkToNewLocation
                .map { (id, _, preText) ->
                    gameState.copy(
                            gameText = GameText(preText, newPlace),
                            currentLocation = id)
                }
                .map { it.updateTextWithItems() }
    }

    private fun describe(gameState: GameState, target: String): GameState {
        val item = gameState.findUsable(target)
                .map {
                    when (it) {
                        is Item -> it.description
                        is FixedItem -> it.description
                    }
                }
                .getOrElse { "There is no $target" }
                .some()

        return gameState.copy(gameText = GameText("You take a closer look.", item))
    }

    private fun take(gameState: GameState, target: String): GameState {

        val gsWithItem = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }
                .getOrElse { gameState }

        val descriptionOpt = gsWithItem.locationOpt().map { it.description }

        val gsOpt: Option<GameState> = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }

        return gsOpt.map { g -> g.copy(gameText = GameText("You take the $target", descriptionOpt)) }
                .getOrElse { gsWithItem.copy(gameText = GameText("There is no $target", descriptionOpt)) }
                .updateTextWithItems()
    }


    fun use(gameState: GameState, target: String): GameState {
        val item = gameState.player.inventory.find { it.name.equals(target, ignoreCase = true) }

        val resetGameState = gameState.resetGameText()

        return if (item == null) resetGameState.appendPretext("You do not have a $target")
        else item.effects
                .map { Free.liftF(it) }
                .reduce { op1, op2 -> op1.flatMap { op2 } }
                .foldMap(interpreterFactory.createInterpreter(resetGameState), Id.monad())
                .ev().value
    }


    private fun eat(gameState: GameState, target: String): GameState {
        return gameState
    }

}

typealias FreeEffect = Free<GameEffect.F, GameState>
fun <A> HK<GameEffect.F, A>.ev(): GameEffect<A> = this as GameEffect<A>

// Free the monads!
sealed class GameEffect<out A> : HK<GameEffect.F, A> {
    sealed class F private constructor()

    data class Describe(val text: String) : GameEffect<GameState>()
    data class KillPlayer(val text: String) : GameEffect<GameState>()
    data class TeleportPlayer(val locationUUID: UUID, val text: String) : GameEffect<GameState>()
    data class OneWayLink(val link: LinkDetails, val text: String) : GameEffect<GameState>()
    data class TwoWayLink(val link: LinkDetails, val returnLink: LinkDetails, val text: String) : GameEffect<GameState>()

    data class LinkDetails(val from: UUID, val to: UUID, val move: Move, val description: String)

    companion object : FreeMonadInstance<F> {
        fun describe(text: String): Free<GameEffect.F, GameState> =
                Free.liftF(Describe(text))

        fun createOneWayLink(link: LinkDetails, text: String): FreeEffect =
                Free.liftF(OneWayLink(link, text))

        fun createTwoWayLink(link: LinkDetails, returnLink: LinkDetails, text: String): FreeEffect =
                Free.liftF(TwoWayLink(link, returnLink, text))

        fun killThePlayer(text: String): FreeEffect =
                Free.liftF(KillPlayer(text))

        fun teleportPlayer(locationUUID: UUID, text: String): FreeEffect =
                Free.liftF(TeleportPlayer(locationUUID, text))
    }
}

class ActionInterpreterFactory() {
    fun createInterpreter(gameState: GameState): FunctionK<GameEffect.F, IdHK> {
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
                } as Id<A>
            }
        }
    }

}
