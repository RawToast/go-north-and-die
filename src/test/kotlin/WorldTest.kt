import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import World.linkPlace
import World.twoWayLinkToPlace

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

    @Test fun placesLinkedToEachOtherAreNavigable() {
        val p1Description = "Starting place"
        val p2Description = "Other place"
        val startPlace = Place(p1Description, setOf())
        val otherPlace = Place(p2Description, setOf())

        val linkedWorld = startPlace.twoWayLinkToPlace(otherPlace,
                Move.EAST, Move.WEST, "You go east", "Back")

        assertEquals(linkedWorld.links.size, 1)
        assertEquals(linkedWorld.links.single().place.links.size, 1)
        assertEquals(linkedWorld.links.single().place.description, p2Description)

        val links = linkedWorld.links.single().place.links.single()
        assertEquals("Back", links.description)
        assertEquals(Move.WEST, links.move)
        assertEquals(p1Description, links.place.description)
    }

    fun basicChecks(p1: Place, p2: Place) {
        assertEquals(p1.description, p2.description)
        assertTrue(p1.links.isEmpty())
        assertTrue(p2.links.size == 1)
        assertEquals("Another place", p2.links.single().place.description)
    }
}
