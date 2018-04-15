package gonorth.free

import arrow.Kind
import arrow.free.Free
import arrow.free.instances.FreeMonadInstance
import gonorth.domain.GameState
import gonorth.domain.Move

typealias FreeEffect = Free<GameEffect.F, GameState>
fun <A> Kind<GameEffect.F, A>.ev(): GameEffect<A> = this as GameEffect<A>

// Free the monads!
sealed class GameEffect<out A> : Kind<GameEffect.F, A> {
    sealed class F private constructor()

    data class Describe(val text: String) : GameEffect<GameState>()
    data class KillPlayer(val text: String) : GameEffect<GameState>()
    data class TeleportPlayer(val locationUUID: String, val text: String) : GameEffect<GameState>()
    data class OneWayLink(val link: LinkDetails, val text: String) : GameEffect<GameState>()
    data class TwoWayLink(val link: LinkDetails, val returnLink: LinkDetails, val text: String) : GameEffect<GameState>()

    data class IncreaseHunger(val amount: Int) : GameEffect<GameState>()
    data class ReduceHunger(val amount: Int) : GameEffect<GameState>()

    data class Destroy(val itemName: String) : GameEffect<GameState>()
    data class RemoveItem(val itemName: String) : GameEffect<GameState>()


    data class LinkDetails(val from: String, val to: String, val move: Move, val description: String)

    companion object : FreeMonadInstance<F> {
        fun describe(text: String): Free<F, GameState> =
                Free.liftF(Describe(text))

        fun createOneWayLink(link: LinkDetails, text: String): FreeEffect =
                Free.liftF(OneWayLink(link, text))

        fun createTwoWayLink(link: LinkDetails, returnLink: LinkDetails, text: String): FreeEffect =
                Free.liftF(TwoWayLink(link, returnLink, text))

        fun killThePlayer(text: String): FreeEffect =
                Free.liftF(KillPlayer(text))

        fun teleportPlayer(locationUUID: String, text: String): FreeEffect =
                Free.liftF(TeleportPlayer(locationUUID, text))

        fun increaseHunger(amount: Int): FreeEffect =
                Free.liftF(IncreaseHunger(amount))

        fun reduceHunger(amount: Int): FreeEffect =
                Free.liftF(ReduceHunger(amount))

        fun destroy(itemName: String): FreeEffect =
                Free.liftF(Destroy(itemName))

        fun remove(itemName: String): FreeEffect =
                Free.liftF(RemoveItem(itemName))
    }
}