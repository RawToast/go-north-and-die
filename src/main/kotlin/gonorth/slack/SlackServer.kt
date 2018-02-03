package gonorth.slack

import arrow.core.Option
import arrow.core.getOrElse
import arrow.syntax.option.toOption
import com.fasterxml.jackson.databind.SerializationFeature
import gonorth.GoNorth
import gonorth.SimpleGameClient
import gonorth.domain.SimpleGameStateGenerator
import gonorth.free.InterpreterFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
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

fun main(args: Array<String>) {
    val port: Int = System.getenv("PORT").toOption().map { Integer.valueOf(it) }.getOrElse { 8080 }
    embeddedServer(Netty, port, watchPaths = listOf("SlackServerKt"), module = Application::module).start()
}

fun Application.module() {

    val interpreter = InterpreterFactory()
    val client = SimpleGameClient(kotlin.collections.mapOf(), GoNorth(interpreter), SimpleGameStateGenerator())

    val slackService = SlackService(client)

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }
    routing {
        get("/health") {
            call.respondText("Hello, world!", ContentType.Text.Html)
        }
        post("create") {
            log.info("Request made to create endpoint")
            if (call.request.contentType() == ContentType.Application.FormUrlEncoded) {
                val formData = call.receiveParameters()

                val userOpt: Option<String> = formData["user_id"].toOption()

                val sr = slackService.createGame(userOpt)

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

                val userOpt: Option<String> = formData["user_id"].toOption()
                val textOpt: Option<String> = formData["text"].toOption()

                val sr = slackService.takeInput(userOpt, textOpt)

                call.respond(sr.getOrElse { SlackResponse("Cannot process request: ${textOpt.getOrElse { "N/A" }}") })
            } else {
                log.warn("Invalid request " + call.request.contentType()
                        + " " + call.request.headers.toString())
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

data class SlackResponse(val text: String)

