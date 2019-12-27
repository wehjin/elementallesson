package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.tomicOf
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
        get("/") { call.respondRedirect("/user/only") }
        route("/user/{user}") {
            val learner = tomic.createLearner()
            get { call.respondHtml { render(learner) } }
            post {
                updateUser(call.receive(), learner)
                call.respondRedirect("/user/only")
            }
            route("plan/{plan}") {
                get {
                    call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                        call.respondHtml { render(learner, plan) }
                    } ?: call.respondRedirect("/user/only")
                }
                post {
                    call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                        updatePlan(plan, call.receive())
                        call.respondRedirect("/user/only/plan/$plan")
                    } ?: call.respondRedirect("/user/only")
                }
            }
        }
    }
}

private fun updateUser(params: Parameters, learner: Peer<Learner.Name, String>) {
    params["add_plan"]?.let { tomic.createPlan(learner.ent, it) }
    params["drop_plan"]?.toLongOrNull()?.let { tomic.deletePlan(learner.ent, it) }
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

private fun HTML.render(learner: Peer<Learner.Name, String>) = body {
    h1 { +" User / ${learner[Learner.Name]} " }
    h2 { +"Studies" }
    ul { +"None" }
    h2 { +"Plans" }
    val plans = tomic.readPlans(learner.ent)
    ul {
        if (plans.isEmpty()) +"None"
        else plans.map { plan ->
            li {
                val planName = plan[Plan.Name]
                val displayName = if (planName?.trim().isNullOrEmpty()) "Untitled" else planName
                a(href = "/user/only/plan/${plan.ent}") { +" $displayName " }
            }
        }
    }
    h4 { +"Add Plan" }
    form(action = "/user/only", method = FormMethod.post) {
        ul {
            textInput {
                name = "add_plan"
                placeholder = "Name"
            }
            +" "
            submitInput { }
        }
    }
}

private fun HTML.render(learner: Peer<Learner.Name, String>, plan: Long) = body {
    h6 { a(href = "/user/only") { +" ${learner[Learner.Name]}" } }
    form(action = "/user/only", method = FormMethod.post) {
        h1 {
            +"Plan / ${plan.toString(16)} "
            hiddenInput {
                name = "drop_plan"
                value = plan.toString()
            }
            submitInput { value = "Drop" }
        }
    }
    h2 { +"Lessons" }
    ol {
        val lessons = tomic.readPlanLessons(plan)
        if (lessons.isEmpty()) {
            +"None"
        } else {
            lessons.map { lesson ->
                li {
                    form("/user/only/plan/$plan", method = FormMethod.post) {
                        val prompt = lesson[Lesson.Prompt]
                        val response = lesson[Lesson.Response]
                        val level = lesson[Lesson.Level] ?: 1
                        +" prompt='$prompt' / response='$response' / level=$level "
                        hiddenInput {
                            name = "drop_lesson"
                            value = "${lesson.ent}"
                        }
                        submitInput { value = "Drop" }
                    }
                }
            }
        }
    }
    h4 { +"Add Lesson" }
    form("/user/only/plan/$plan", method = FormMethod.post) {
        ul {
            p {
                textInput {
                    name = "lesson_prompt"
                    placeholder = "Prompt"
                }
            }
            p {
                textInput {
                    name = "lesson_response"
                    placeholder = "Response"
                }
            }
            p {
                textInput {
                    name = "lesson_level"
                    placeholder = "Level"
                }
            }
        }
        p { submitInput { name = "add_lesson" } }
    }
}
