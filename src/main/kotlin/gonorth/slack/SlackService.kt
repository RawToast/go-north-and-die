package gonorth.slack

import gonorth.GameClient
import gonorth.domain.locationOpt
import kategory.Option
import kategory.getOrElse

class SlackService(private val client: GameClient) {

    private val nl = "\n"

    private val empty = ""

    fun createGame(user: Option<String>): Option<SlackResponse> {
        return user.map { u -> client.startGame(u) }
                .map { g ->
                    val mvs: List<String> = g.world.links
                            .getOrDefault(g.currentLocation, emptySet())
                            .map { it.move.name }
                    SlackResponse(g.gameText.preText + nl +
                            g.gameText.description.map { it + nl }.getOrElse { empty } +
                            g.locationOpt().map { it.description.plus(nl) }.getOrElse { empty } +
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
                        SlackResponse(g.gameText.preText + nl +
                                g.gameText.description.map { it + nl }.getOrElse { empty })
                    } else {
                        SlackResponse(g.gameText.preText + nl +
                                g.gameText.description.map { it + nl }.getOrElse { empty }
                                + nl
                                + mvs
                                )
                    }
                }
    }
}