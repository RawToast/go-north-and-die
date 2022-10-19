package gonorth
//
//import arrow.core.*
//import arrow.free.Free
//import arrow.free.flatMap
//import arrow.free.foldMap
//import gonorth.domain.*
//import gonorth.free.InterpreterFactory
//import java.util.Random
//
//class GoNorth(private val interpreterFactory: InterpreterFactory) {
//
//    fun takeAction(gameState: GameState, move: Move, command: Option<String>): GameState {
//
//        val withNewSeed = gameState.copy(seed = Random(gameState.seed).nextLong())
//
//        return when {
//            withNewSeed.player.alive -> handleActionWithTarget(move, withNewSeed, command.getOrElse { "" })
//            else -> gameState
//        }
//    }
//
//    private fun handleActionWithTarget(move: Move, gameState: GameState, target: String): GameState = when (move) {
//        Move.NORTH -> handleMovement(gameState, move).getOrElse { gameState }
//        Move.EAST -> handleMovement(gameState, move).getOrElse { gameState }
//        Move.SOUTH -> handleMovement(gameState, move).getOrElse { gameState }
//        Move.WEST -> handleMovement(gameState, move).getOrElse { gameState }
//        Move.DESCRIBE -> describe(gameState, target)
//        Move.TAKE -> take(gameState, target)
//        Move.USE -> use(gameState, target)
//        Move.EAT -> eat(gameState, target)
//    }
//
//    private fun handleMovement(gameState: GameState, move: Move): Option<GameState> {
//
//        val linkToNewLocation: Option<Link> = gameState.locationOpt()
//                .flatMap { gameState.fetchLinks(it.id) }
//                .flatMap { it.find { it.move == move }.toOption() }
//
//        val newPlace = linkToNewLocation
//                .flatMap { gameState.findLocation(it.to) }
//                .map { it.description }
//
//        return linkToNewLocation
//                .map { (id, _, preText) ->
//                    gameState.copy(
//                            gameText = GameText(preText, newPlace),
//                            currentLocation = id)
//                }
//                .map { it.updateTextWithItems() }
//    }
//
//    private fun describe(gameState: GameState, target: String): GameState {
//        val item = gameState.findUsable(target)
//                .map {
//                    when (it) {
//                        is Item -> it.description
//                        is FixedItem -> it.description
//                    }
//                }
//                .getOrElse { "There is no $target" }
//                .some()
//
//        return gameState.copy(gameText = GameText("You take a closer look.", item))
//    }
//
//    private fun take(gameState: GameState, target: String): GameState {
//        val gameStateAfterTakingItem: Option<GameState> = gameState.moveItemToInventory(target)
//
//        val descriptionOpt = gameStateAfterTakingItem.getOrElse { gameState }.currentDescription()
//
//        return gameStateAfterTakingItem
//                .map { g -> g.updateGameText("You take the $target", descriptionOpt) }
//                .getOrElse { gameState.updateGameText("There is no $target", descriptionOpt) }
//                .updateTextWithItems()
//    }
//
//    fun use(gameState: GameState, target: String): GameState {
//
//        val usedItem = gameState.findPossibleUseable(target)
//
//        val resetGameState = gameState.resetGameText()
//
//        return usedItem.map {
//            val effects = it.effects()
//            when (effects) {
//                is FixedEffects -> effects.effects
//                is RandomEffects -> effects.fetchEffect(gameState.seed)
//            }.map { Free.liftF(it) }
//                    .reduce { op1, op2 -> op1.flatMap { op2 } }
//                    .foldMap(interpreterFactory.impureGameEffectInterpreter(resetGameState), Id.monad())
//                    .fix().value
//
//        }.getOrElse { resetGameState.appendPretext("You do not have a $target") }
//    }
//
//
//    private fun eat(gameState: GameState, target: String): GameState {
//        return gameState
//    }
//
//}
