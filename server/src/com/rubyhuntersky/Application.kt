package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.tomicOf
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

private val homeDir = System.getenv("HOME")
private val appDir = File(homeDir, ".studycatastrophe")
private val tomeDir = File(appDir, "tome")
val tomic = tomicOf(tomeDir) { emptyList() }

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

    install(CallLogging)

    routing {
        get("/") { call.respondRedirect("/user/only") }
        route("/user/{user}") {
            val learner = tomic.createLearner()
            get { call.respondHtml { renderLearner(learner) } }
            post {
                val params: Parameters = call.receive()
                log.trace("Parameters: $params")
                params["add_plan"]?.let { tomic.createPlan(learner.ent, it) }
                params["drop_plan"]?.toLongOrNull()?.let { tomic.deletePlan(learner.ent, it) }
                params["add_study"]?.let { tomic.createStudy(learner.ent, it) }
                params["drop_study"]?.toLongOrNull()?.let { tomic.deleteStudy(learner.ent, it) }
                call.respondRedirect("/user/only")
            }
            route("plan/{plan}") {
                get {
                    call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                        call.respondHtml { renderPlan(learner, plan) }
                    } ?: call.respondRedirect("/user/only")
                }
                post {
                    call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                        updatePlan(plan, call.receive())
                        call.respondRedirect("/user/only/plan/$plan")
                    } ?: call.respondRedirect("/user/only")
                }
            }
            route("study/{study}") {
                get {
                    val maybeStudy = call.parameters["study"]?.toLongOrNull()?.let { ent ->
                        tomic.readStudies(learner.ent).firstOrNull { it.ent == ent }
                    }
                    maybeStudy?.let { study ->
                        call.respondHtml { renderStudy(learner, study) }
                    } ?: call.respondRedirect("/user/only")
                }
            }
        }
    }
}

private fun updatePlan(plan: Long, params: Parameters) {
    params["add_lesson"]?.let {
        val prompt = params["lesson_prompt"]?.trim() ?: "Who am I?"
        val response = params["lesson_response"]?.trim() ?: "I am your father."
        val level = params["lesson_level"]?.toLongOrNull() ?: 1L
        tomic.createPlanLesson(plan, prompt, response, level)
    }
    params["drop_lesson"]?.toLongOrNull()?.let {
        tomic.deletePlanLesson(plan, it)
    }
}

