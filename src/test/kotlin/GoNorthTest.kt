
import kotlin.test.assertEquals
import org.junit.Test

class GoNorthTest {
    val gn = GoNorth()

    @Test fun northTest() {
        assertEquals("You died", gn.move(Move.NORTH))
    }

    @Test fun eastTest() {
        assertEquals("You died", gn.move(Move.NORTH))
    }
}