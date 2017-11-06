
import kotlin.test.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class WorldTest {

    @Test fun linkingPlacesUpdatesBothPlacesLinks() {
        val p1 = Place("Starting place", setOf(), mapOf())
        val p2 = World.addNewPlace("Another place",
                p1, mapOf(Pair(Move.NORTH, "You walk to another place")))

        assert(p1 != p2)
        basicChecks(p1, p2)

    }

    @Test fun updatedPlacesContainValidLinkIds() {
        val p1 = Place("Starting place", setOf(), mapOf())
        val p2 = World.addNewPlace("Another place", p1,
                mapOf(Pair(Move.NORTH, "You walk to another place")))

        basicChecks(p1, p2)

        assertTrue(p1.moves.isEmpty())
        assertEquals(Move.NORTH, p2.moves.keys.first())
    }

    fun basicChecks(p1: Place, p2: Place) {
        assertEquals(p1.description, p2.description)
        assertTrue(p1.linkedPlaces.isEmpty())
        assertTrue(p2.linkedPlaces.size == 1)
        assertEquals("Another place", p2.linkedPlaces.single().description)
        assertEquals(1, p2.linkedPlaces.single().linkedPlaces.size)
    }

}