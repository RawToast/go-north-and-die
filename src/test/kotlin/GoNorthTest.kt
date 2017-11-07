
import org.junit.Test

class GoNorthTest {
    val goNorth = GoNorth()

    val place1 = World.addNewPlace("You went north and died",
            Place("Starting place", setOf()),
            Move.NORTH, "You stumble ahead")
    val place2 = World.addNewPlace("and won!", place1,
            Move.EAST, "You head east...")

    val gameState = GameState("You venture into a dark dungeon", place2)

    @Test fun thePlayerStartsAtTheStartingPlace() {
        assert(gameState.preText == "You venture into a dark dungeon")
        assert(gameState.place.description == "Starting place")
    }

    @Test fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, Move.NORTH)

        assert(newState.preText.contains("You stumble ahead"))
        assert(newState.place.links.isEmpty())
        assert(newState.place.description.contains("You went north and died"))
    }

    @Test fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, Move.EAST)

        assert(newState.preText.contains("You head east..."))

        assert(newState.place.links.isEmpty())
        assert(newState.place.description.contains("and won!"))
    }

}