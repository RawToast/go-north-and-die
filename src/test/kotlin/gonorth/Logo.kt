package gonorth;


import kategory.*;
import org.junit.Test
import kotlin.test.assertEquals


sealed class Logo<out A> : HK<Logo.F, A> {
    sealed class F private constructor()

    data class MoveForward(val position: Position, val times: Int) : Logo<Position>()
    data class TurnRight(val position: Position) : Logo<Position>()
    data class TurnLeft(val position: Position) : Logo<Position>()
    data class ShowLocation(val position: Position) : Logo<Position>()

    companion object : FreeMonadInstance<Logo.F> {
        fun moveForward(position: Position, times: Int): Free<Logo.F, Position> =
                Free.liftF(MoveForward(position, times))

        fun turnLeft(position: Position): Free<Logo.F, Position> =
                Free.liftF(TurnLeft(position))

        fun turnRight(position: Position): Free<Logo.F, Position> =
                Free.liftF(TurnRight(position))

        fun showPosition(position: Position): Free<Logo.F, Position> =
                Free.liftF(ShowLocation(position))
    }
}

data class Position(val x: Int, val y: Int, val facing: Direction)

enum class Direction { NORTH, EAST, SOUTH, WEST }

class LogoTest {
    @Test
    fun logoTests() {
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

        val goRight = { p: Position -> Logo.turnRight(p)}


        val ffs: List<(Position) -> Free<Logo.F, Position>> = listOf(1,2,3).map { goRight }




        fun <A> HK<Logo.F, A>.ev(): Logo<A> = this as Logo<A>
        val idInterpreter: FunctionK<Logo.F, IdHK> = object : FunctionK<Logo.F, IdHK> {
            override fun <A> invoke(fa: HK<Logo.F, A>): Id<A> {

                val op = fa.ev()
                return when (op) {
                    is Logo.MoveForward ->
                        when (op.position.facing) {
                            Direction.NORTH -> Id(op.position.copy(y=+op.times))
                            Direction.EAST -> Id(op.position.copy(x=+op.times))
                            Direction.SOUTH -> Id(op.position.copy(y=-op.times))
                            Direction.WEST -> Id(op.position.copy(x=-op.times))
                        }
                    is Logo.TurnRight -> {
                        fun setFaceing
                                (d: Direction) = Id(op.position.copy(facing = d))
                        when (op.position.facing) {
                            Direction.NORTH -> Id(op.position.copy(facing = Direction.EAST))
                            Direction.EAST -> setFaceing(Direction.SOUTH)
                            Direction.SOUTH -> setFaceing(Direction.WEST)
                            Direction.WEST -> setFaceing(Direction.NORTH)
                        }
                    }
                    is Logo.TurnLeft -> {
                        fun setFaceing(d: Direction) = Id(op.position.copy(facing = d))
                        when (op.position.facing) {
                            Direction.NORTH -> setFaceing(Direction.WEST)
                            Direction.EAST -> setFaceing(Direction.NORTH)
                            Direction.SOUTH -> setFaceing(Direction.EAST)
                            Direction.WEST -> setFaceing(Direction.SOUTH)
                        }
                    }
                    is Logo.ShowLocation -> Id(op.position)
                } as Id<A>

            }
        }

        val result1: Id<Position> = method1.foldMap(idInterpreter, Id.monad()).ev()
        val result2 = method2.foldMap(idInterpreter, Id.monad() ).ev()

        assertEquals(Direction.NORTH , result1.value.facing)
        assertEquals( -3, result1.value.x)
        assertEquals( 0,result1.value.y)

        assertEquals(Direction.SOUTH, result2.value.facing)
    }
}