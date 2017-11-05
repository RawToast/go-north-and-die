class GoNorth {

    fun takeAction(gameState: GameState, m: Move): GameState {

        val newText = when(m) {
            Move.NORTH -> "You went north and died"
            Move.EAST -> "You went east and won"
        }

        return gameState.copy(gameText = newText)
    }
}

enum class Move {
    NORTH,
    EAST
}

data class GameState(val gameText: String, val actions: List<Move>)


