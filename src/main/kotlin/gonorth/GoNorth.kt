package gonorth

import arrow.core.*
import arrow.free.Free
import arrow.free.flatMap
import arrow.free.foldMap
import arrow.syntax.collections.firstOption
import arrow.syntax.monad.flatten
import arrow.syntax.option.some
import arrow.syntax.option.toOption
import gonorth.domain.*
import gonorth.free.InterpreterFactory

class GoNorth(private val interpreterFactory: InterpreterFactory) {

    fun takeAction(gameState: GameState, move: Move, command: Option<String>): GameState {
        return if (gameState.player.alive) {
            handleActionWithTarget(move, gameState, command.getOrElse { "" })
        } else {
            gameState
        }
    }

    private fun handleActionWithTarget(move: Move, gameState: GameState, target: String): GameState = when (move) {
        Move.NORTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.EAST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.SOUTH -> handleMovement(gameState, move).getOrElse { gameState }
        Move.WEST -> handleMovement(gameState, move).getOrElse { gameState }
        Move.DESCRIBE -> describe(gameState, target)
        Move.TAKE -> take(gameState, target)
        Move.USE -> use(gameState, target)
        Move.EAT -> eat(gameState, target)
    }

    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
        val linkToNewLocation: Option<Link> = gameState.locationOpt()
                .flatMap { gameState.fetchLinks(it.id) }
                .flatMap { it.find { it.move == move }.toOption() }

        val newPlace = linkToNewLocation
                .flatMap { gameState.findLocation(it.to) }
                .map { it.description }

        return linkToNewLocation
                .map { (id, _, preText) ->
                    gameState.copy(
                            gameText = GameText(preText, newPlace),
                            currentLocation = id)
                }
                .map { it.updateTextWithItems() }
    }

    private fun describe(gameState: GameState, target: String): GameState {
        val item = gameState.findUsable(target)
                .map {
                    when (it) {
                        is Item -> it.description
                        is FixedItem -> it.description
                    }
                }
                .getOrElse { "There is no $target" }
                .some()

        return gameState.copy(gameText = GameText("You take a closer look.", item))
    }

    private fun take(gameState: GameState, target: String): GameState {

        val gsWithItem = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }
                .getOrElse { gameState }

        val descriptionOpt = gsWithItem.locationOpt().map { it.description }

        val gsOpt: Option<GameState> = gameState.findItem(target)
                .map { gameState.removeItem(it.name).addToInventory(it) }

        return gsOpt.map { g -> g.copy(gameText = GameText("You take the $target", descriptionOpt)) }
                .getOrElse { gsWithItem.copy(gameText = GameText("There is no $target", descriptionOpt)) }
                .updateTextWithItems()
    }


    fun use(gameState: GameState, target: String): GameState {

        val worldItem = gameState.locationOpt()
                .flatMap { it.items.findUsable(target) }

        val item = gameState.player.inventory.findUsable(target)

        val usedItem = listOf(worldItem, item).firstOption { it.nonEmpty() }.flatten().ev()

        val resetGameState = gameState.resetGameText()

            return usedItem.map {
            when (it) {
                is Item -> it.effects
                is FixedItem -> it.effects
            }.map { Free.liftF(it) }
                    .reduce { op1, op2 -> op1.flatMap { op2 } }
                    .foldMap(interpreterFactory.impureGameEffectInterpreter(resetGameState), Id.monad())
                    .ev().value
        }.getOrElse { resetGameState.appendPretext("You do not have a $target") }
    }


    private fun eat(gameState: GameState, target: String): GameState {
        return gameState
    }

}
