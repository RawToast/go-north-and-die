
import kotlin.test.assertEquals
import org.junit.Test

class GoNorthTest {
    val gn = GoNorth()


    @Test fun whenGivenTheCommandNorthThePlayerDies() {

        val gs = GameState("text", listOf(Move.NORTH))

        val newGs = gn.takeAction(gs, Move.NORTH)

        assertEquals("You went north and died", newGs.gameText)
    }

    @Test fun whenGivenTheCommandEastThePlayerWins() {

        val gs = GameState("text", listOf(Move.EAST))

        val newGs = gn.takeAction(gs, Move.EAST)

        assertEquals("You went east and won", newGs.gameText)
    }

}