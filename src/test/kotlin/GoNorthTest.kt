
import org.junit.Test
import World.linkNewPlace
import kotlin.test.assertEquals

class GoNorthTest {
    val goNorth = GoNorth()

    val startingPlace = Place("Starting place", setOf())
            .linkNewPlace(Move.NORTH, "You stumble ahead", "You went north and died")
            .linkNewPlace(Move.EAST, "You head east...", "and won!")

    val gameState = GameState("You venture into a dark dungeon", startingPlace)

    @Test fun thePlayerStartsAtTheStartingPlace() {
        assert(gameState.preText == "You venture into a dark dungeon")
        assert(gameState.place.description == "Starting place")
    }

    @Test fun whenGivenNorthThePlayerDies() {
        val newState = goNorth.takeAction(gameState, Move.NORTH)

        assertEquals("You stumble ahead", newState.preText)
        assert(newState.place.links.isEmpty())
        assert(newState.place.description.contains("You went north and died"))
    }

    @Test fun whenGivenEastThePlayerWins() {
        val newState = goNorth.takeAction(gameState, Move.EAST)

        assertEquals("You head east...", newState.preText)

        assert(newState.place.links.isEmpty())
        assert(newState.place.description.contains("and won!"))
    }
}
