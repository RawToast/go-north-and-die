package gonorth;


import kategory.*;
import org.junit.Test
import kotlin.test.assertEquals


sealed class Counting<out A> : HK<Counting.F, A> {
    sealed class F private constructor()

    data class Increment(val value: Int) : Counting<Int>()
    data class Decrement(val value: Int) : Counting<Int>()
    data class Init(val initial: Int=0) : Counting<Int>()
//    data class Fetch(val b:Unit=Unit) : Counting<Int>()

    companion object : FreeMonadInstance<Logo.F> {
        fun increment(value: Int): Free<F, Int> =
                Free.liftF(Increment(value))

        fun decrement(value: Int): Free<F, Int> =
                Free.liftF(Decrement(value))


        fun init(value: Int): Free<F, Int> =
                Free.liftF(Init(value))

//        fun fetch(value: Unit=Unit): Free<F, Int> =
//                Free.liftF(Fetch(value))
    }
}

class CountingTest {
    @Test
    fun logoTests() {


        val step1 = Counting.init(1)
        val step2 = Counting.increment(3)
        val step3 = Counting.decrement(2)
        val step4 = Counting.init(0)
//        val step5 = Counting.fetch()


//        val fmapProgram: Free<Counting.F, Int> =
//                step1.flatMap { _:Unit ->
//                    step2.flatMap { _: Unit ->
//                        step3.flatMap { _ ->
//                            step4.flatMap { _ ->
//                                step5 } } } }
//                        .ev()

        val newFMapProg = step1.flatMap { _ ->
                            step2.flatMap { _ ->
                                step3.flatMap { _ ->
                                    step4 }
                                }
                            }



        val reduczeProram: Free<Counting.F, Int> = listOf(step1, step2, step3, step4)
                .reduce( { op1, op2 -> op1.flatMap { op2 }})
                .ev()



        val startPosition = Position(0, 0, Direction.NORTH)

        val res1: Free<Logo.F, Position> = Logo.showPosition(startPosition)

        val method1: Free<Logo.F, Position> = res1.flatMap { p: Position -> Logo.turnLeft(p) }
                .flatMap { p -> Logo.moveForward(p, 3) }
                .flatMap { p -> Logo.turnRight(p) }
                .flatMap { p -> Logo.showPosition(p) }
                .ev()

        val method2: Free<Logo.F, Position> = res1.flatMap { Logo.turnLeft(it) }
                .flatMap { Logo.moveForward(it, 3) }
                .flatMap { Logo.turnRight(it) }
                .flatMap { Logo.turnRight(it) }
                .flatMap { Logo.turnRight(it) }
                .flatMap { Logo.showPosition(it) }
                .ev()


        val result1: Id<Position> = method1.foldMap(null, Id.monad()).ev()

    }
}