import kategory.Option
import kategory.Option.None
import kategory.Option.Some

class GoNorth {

    fun takeAction(gameState: GameState, m: Move): GameState
    {
        val optState: Option<GameState> = Option.fromNullable(
            gameState.place.links
                .find { it.move == m })
                .map { (place, _, preText) -> GameState(preText, place)
        }

        return when(optState) {
                is Some -> optState.value
                is None -> gameState
        }
    }

}

enum class Move {
    NORTH,
    EAST
}

data class GameState(val preText: String, val place: Place)

