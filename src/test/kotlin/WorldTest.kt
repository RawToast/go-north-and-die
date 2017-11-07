
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldTest {

    @Test fun linkingPlacesUpdatesBothPlacesLinks() {
        val p1 = Place("Starting place", setOf())
        val p2 = World.addNewPlace("Another place",
                p1, Move.NORTH, "You walk to another place")

        assert(p1 != p2)
        basicChecks(p1, p2)
    }

    @Test fun updatedPlacesContainValidLinkIds() {
        val p1 = Place("Starting place", setOf())
        val p2 = World.addNewPlace("Another place", p1,
                Move.NORTH, "You walk to another place")

        basicChecks(p1, p2)

        assertTrue(p1.links.isEmpty())
        assertEquals(Move.NORTH, p2.links.single().move)
    }

    fun basicChecks(p1: Place, p2: Place) {
        assertEquals(p1.description, p2.description)
        assertTrue(p1.links.isEmpty())
        assertTrue(p2.links.size == 1)
        assertEquals("Another place", p2.links.single().place.description)
    }

}