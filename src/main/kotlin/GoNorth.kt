class GoNorth {

    fun move(m:Move): String {
        return when(m) {
            Move.NORTH -> "You died"
            Move.EAST -> "You win"
        }
    }
}

enum class Move {
    NORTH,
    EAST
}