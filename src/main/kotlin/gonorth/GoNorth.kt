package gonorth

import gonorth.domain.*
import kategory.*
import java.util.*

class GoNorth {

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
                .flatMap { it.find { it.move == move }.toOpt() }

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
        val item = gameState.findItem(target)
                .map { it.description }
                .getOrElse { "There is no $target" }
                .some()

        return gameState.copy(gameText = GameText("You take a closer look.", item))
    }

    private fun take(gameState: GameState, target: String): GameState {

        val gsWithItem = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }
                .getOrElse { gameState }

        val descriptionOpt = gsWithItem.locationOpt().map { it.description }
        val text = GameText("You take the $target", descriptionOpt)

        return gsWithItem.copy(gameText = text)
                .updateTextWithItems()
    }


    private fun use(gameState: GameState, target: String): GameState {
        val rr: GameEffect<GameState> = GameEffect.KillPlayer("You bash yourself on the head with the $target")
        val rr2: GameEffect<GameState> = GameEffect.KillPlayer("You bash yourself on the head with the $target")


        val mylist: List<GameEffect<GameState>> = listOf(rr, rr2)

        val liftedList = listOf(rr, rr2).map { Free.liftF(it) }

        mylist.reduce({op1, op2 -> op1})

//        liftedList.reduce({op1, op2 -> op1.flatMap { g -> op2 }()})

        val freeR = Free.liftF(rr)
        val freeS = Free.liftF(rr2)

        val xy = freeR.flatMap { gs -> freeS }


        val listOfStuff: List<FreeEffect> = emptyList()

        fun toFree(text: String): (GameState) -> GameEffect.KillPlayer = { gs: GameState -> GameEffect.KillPlayer(gs, text) }

        val frees = listOf("Turnip", "Banana").map { toFree(it) }

        val cc = toFree("Cool")(gameState)



        return gameState
    }

    private fun eat(gameState: GameState, target: String): GameState {
        return gameState
    }

}

typealias FreeEffect = Free<GameEffect.F, GameState>

// Free the monads!
sealed class GameEffect<out A> : HK<GameEffect.F, A> {
    sealed class F private constructor()


    data class KillPlayer(val text: String) : GameEffect<GameState>()
    data class TeleportPlayer(val locationUUID: UUID, val text: String) : GameEffect<GameState>()
    data class OneWayLink(val link: LinkDetails, val text: String) : GameEffect<GameState>()
    data class TwoWayLink(val link: LinkDetails, val returnLink: LinkDetails, val text: String) : GameEffect<GameState>()

    data class LinkDetails(val from: UUID, val to: UUID, val move: Move, val description: String)

    companion object : FreeMonadInstance<GameEffect.F> {
        fun createOneWayLink(link: LinkDetails, text: String): Free<GameEffect.F, GameState> =
                Free.liftF(OneWayLink(link, text))

        fun createTwoWayLink(link: LinkDetails, returnLink: LinkDetails, text: String): Free<GameEffect.F, GameState> =
                Free.liftF(TwoWayLink(link, returnLink, text))

        fun killThePlayer(text: String): Free<GameEffect.F, GameState> =
                Free.liftF(KillPlayer(text))

        fun teleportPlayer(locationUUID: UUID, text: String): Free<GameEffect.F, GameState> =
                Free.liftF(TeleportPlayer(locationUUID, text))
    }
}
