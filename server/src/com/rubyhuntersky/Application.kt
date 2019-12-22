package com.rubyhuntersky

import com.rubyhuntersky.data.v2.Learner
import com.rubyhuntersky.data.v2.createLearner
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.tomicOf
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import kotlinx.html.*
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

private val homeDir = System.getenv("HOME")
private val appDir = File(homeDir, ".studycatastrophe")
private val tomeDir = File(appDir, "tome")
private val tomic = tomicOf(tomeDir) { emptyList() }

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = "/ktor/application/shutdown"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/user/only") {
            val learner = tomic.createLearner()
            call.respondHtml {
                body {
                    h1 { +"User: ${learner[Learner.Name]?.capitalize()}" }
                    h2 { +"Studies" }
                    p {
                        textInput {
                            name = "Study Name"
                            id = "study_name"
                            value = "New Study"
                        }
                        button { +"Add" }
                    }
                    h2 { +"Plans" }
                    ul {
                        +"None"
                    }
                }
            }
        }
    }
}
