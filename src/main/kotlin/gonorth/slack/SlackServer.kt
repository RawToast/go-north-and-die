package gonorth.slack

import com.fasterxml.jackson.databind.SerializationFeature
import gonorth.GoNorth
import gonorth.SpikeGameClient
import gonorth.domain.location
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.contentType
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kategory.Option
import kategory.empty
import kategory.getOrElse

fun main(args: Array<String>) {
    val port: Int = System.getenv("PORT").toOpt().map { Integer.valueOf(it) }.getOrElse { 8080 }
    embeddedServer(Netty, port, watchPaths = listOf("SlackServerKt"), module = Application::module).start()
}

fun Application.module() {

    val client = SpikeGameClient(kotlin.collections.mapOf(), GoNorth())

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }
    routing {
        get("/") {
            call.respondText("Hello, world!", ContentType.Text.Html)
        }
        post("create") {
            log.info("Request made to create endpoint")

            if (call.request.contentType() == ContentType.Application.FormUrlEncoded) {
                val formData = call.receiveParameters()

                val userOpt: Option<String> = formData["user_id"].toOpt()

                val sr = userOpt.map { u -> client.startGame(u) }
                        .map { g ->
                            val mvs: List<String> = g.world.links
                                    .getOrDefault(g.currentLocation, emptySet())
                                    .map { it.move.name }
                            SlackResponse(g.preText.preText + "\n" +
                                    g.preText.description.map { it + "\n"}.getOrElse { "" } +
                                    mvs)
                        }



                call.respond(sr
                        .getOrElse { SlackResponse("Failed to create game") })
            } else {

                log.warn("Invalid request " + call.request.contentType()
                        + " " + call.request.headers.toString())
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("move") {
            log.info("Request made to move endpoint")

            if (call.request.contentType() == ContentType.Application.FormUrlEncoded) {
                val formData = call.receiveParameters()

                val userOpt: Option<String> = formData["user_id"].toOpt()
                val textOpt: Option<String> = formData["text"].toOpt()

                val sr = userOpt
                        .flatMap { u ->
                            textOpt.flatMap { t -> client.takeInput(u, t) }
                        }
                        .map { g ->

                            val mvs: List<String> = g.world.links
                                    .getOrDefault(g.currentLocation, emptySet())
                                    .map { it.move.name }

                            if (mvs.isEmpty()) {
                                SlackResponse(g.preText.preText + "\n" +
                                        g.preText.description.map { it + "\n"}.getOrElse { "" } +
                                        g.location()?.description)
                            } else {
                                SlackResponse(g.preText.preText + "\n" +
                                        g.preText.description.map { it + "\n"}.getOrElse { "" } +
                                        g.location()?.description + "\n" +
                                        mvs)
                            }
                        }

                call.respond(sr.getOrElse { SlackResponse("Unprocessable request: ${textOpt.getOrElse { "N/A" }}") })
            } else {
                log.warn("Invalid request " + call.request.contentType()
                        + " " + call.request.headers.toString())
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

data class SlackResponse(val text: String)

fun <T> T?.toOpt(): Option<T> {
    return Option.fromNullable(this)
}

