package gonorth.slack

import gonorth.GameClient
import gonorth.domain.location
import kategory.Option
import kategory.getOrElse

class SlackService(val client: GameClient) {

    fun createGame(user: Option<String>): Option<SlackResponse> {
        return user.map { u -> client.startGame(u) }
                .map { g ->
                    val mvs: List<String> = g.world.links
                            .getOrDefault(g.currentLocation, emptySet())
                            .map { it.move.name }
                    SlackResponse(g.preText.preText + "\n" +
                            g.preText.description.map { it + "\n" }.getOrElse { "" } +
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
                                g.preText.description.map { it + "\n" }.getOrElse { "" } +
                                g.location()?.description)
                    } else {
                        SlackResponse(g.preText.preText + "\n" +
                                g.preText.description.map { it + "\n" }.getOrElse { "" } +
                                g.location()?.description + "\n" +
                                mvs)
                    }
                }
    }
}