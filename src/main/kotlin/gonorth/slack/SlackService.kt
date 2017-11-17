package gonorth.slack

import gonorth.GameClient
import gonorth.domain.locationOpt
import kategory.Option
import kategory.getOrElse

class SlackService(private val client: GameClient) {

    fun createGame(user: Option<String>): Option<SlackResponse> {
        return user.map { u -> client.startGame(u) }
                .map { g ->
                    val mvs: List<String> = g.world.links
                            .getOrDefault(g.currentLocation, emptySet())
                            .map { it.move.name }
                    SlackResponse(g.preText.preText + "\n" +
                            g.preText.description.map { it + "\n" }.getOrElse { "" } +
                            g.locationOpt().map { it.description.plus("\n")}.getOrElse {""} +
                            mvs)
                }
    }

    fun takeInput(user: Option<String>, input: Option<String>): Option<SlackResponse> {
        return user
                .flatMap { u ->
                    input.flatMap { t -> client.takeInput(u, t) }
                }
                .map { g ->

                    val mvs: List<String> = g.world.links
                            .getOrDefault(g.currentLocation, emptySet())
                            .map { it.move.name }

                    if (mvs.isEmpty()) {
                        SlackResponse(g.preText.preText + "\n" +
                                g.preText.description.map { it + "\n" }.getOrElse { "" })
                    } else {
                        SlackResponse(g.preText.preText + "\n" +
                                g.preText.description.map { it + "\n" }.getOrElse { "" } +
                                mvs)
                    }
                }
    }
}