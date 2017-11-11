import gonorth.domain.Location
import gonorth.world.WorldBuilder
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import gonorth.domain.Move
import java.util.*

class WorldBuilderTest {

    val p1Description = "Starting to"
    val p2Description = "Other to"
    val p1UUID = UUID.randomUUID()
    val p2UUID = UUID.randomUUID()
    val p1 = Location(p1UUID, p1Description)
    val p2 = Location(p2UUID, p2Description)
    val worldBuilder = WorldBuilder().newLocation(p1)

    @Test fun linkingPlacesUpdatesBothPlacesLinks() {

        val world = worldBuilder.newLocation(p2)
                .linkLocation(p1, p2, Move.NORTH, "You walk to another to")
                .world

        assertEquals(2, world.locations.size, "gonorth.domain.World should have two locations")
        assertEquals(1, world.links.size, "gonorth.domain.World should contain one link")
        assert(p1 != p2)
        assertTrue(world.links.containsKey(p1UUID))
        assertTrue(!world.links.containsKey(p2UUID))
        assertEquals(1, world.links[p1UUID]?.size)
    }

    @Test fun placesLinkedToEachOtherAreNavigable() {

        val world = worldBuilder.newLocation(p2)
                .twoWayLink(p1, p2, Move.EAST, Move.WEST,
                        "You go east", "Back").world

        assertEquals(2, world.locations.size, "gonorth.domain.World should have two locations")
        assertEquals(2, world.links.size, "gonorth.domain.World should contain two links")

        assertTrue(world.links.containsKey(p1UUID), "Place 1 has a link")
        assertTrue(world.links.containsKey(p2UUID), "Place 2 has a link")

        assertEquals(1, world.links[p1UUID]!!.size)
        assertEquals(p2UUID, world.links[p1UUID]!!.single().to )
        assertEquals(Move.EAST, world.links[p1UUID]!!.single().move)

        assertEquals(1, world.links[p2UUID]!!.size)
        assertEquals(p2UUID, world.links[p1UUID]!!.single().to )
        assertEquals(Move.WEST, world.links[p2UUID]!!.single().move)
    }
}

object WorldExtensions {



}