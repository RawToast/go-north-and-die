package gonorth

import kategory.Option
import kategory.getOrElse
import kategory.nonEmpty
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class UtilTest {

    @Test
    fun turnsNullIntoNone(){
        val i: Int? = null
        val x: Option<Int> = i.toOpt()


        assertTrue(x.isEmpty, "Null should create an empty Option")
    }

    @Test
    fun turnsNullableValueIntoSome(){
        val i: Int? = 3
        val x: Option<Int> = i.toOpt()


        assertTrue(x.nonEmpty(), "When the nullable has a value, it should create a Option holding a value")
        assertEquals(i?.or(1), x.getOrElse { 0 })
    }

}

