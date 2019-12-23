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
        route("/user/only") {
            get {
                val learner = tomic.createLearner()
                call.respondHtml { render(learner) }
            }
            route("plan") {
                route("{plan}") {
                    get {
                        val learner = tomic.createLearner()
                        call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                            call.respondHtml { render(learner, plan) }
                        } ?: call.respondRedirect("/user/only")
                    }
                    route("lesson") {
                        post {
                            call.parameters["plan"]?.toLongOrNull()?.let { plan ->
                                tomic.createLearner()
                                val params = call.receive<Parameters>()
                                params["add_lesson"]?.let {
                                    val prompt = params["lesson_prompt"]?.trim() ?: "Who am I?"
                                    val response = params["lesson_response"]?.trim() ?: "I am your father."
                                    tomic.createPlanLesson(plan, prompt, response)
                                }
                                params["drop_lesson"]?.toLongOrNull()?.let {
                                    tomic.deletePlanLesson(plan, it)
                                }
                                call.respondRedirect("/user/only/plan/$plan")
                            } ?: call.respondRedirect("/user/only")
                        }
                    }
                }
                post {
                    val learner = tomic.createLearner()
                    val params = call.receive<Parameters>()
                    params["add_plan"]?.let {
                        tomic.createPlan(learner.ent, it)
                    }
                    params["drop_plan"]?.toLongOrNull()?.let {
                        tomic.deletePlan(learner.ent, it)
                    }
                    call.respondRedirect("/user/only")
                }
            }
        }
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
    form(action = "/user/only/plan", method = FormMethod.post) {
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
    form(action = "/user/only/plan", method = FormMethod.post) {
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
                    form(
                        action = "/user/only/plan/$plan/lesson",
                        method = FormMethod.post
                    ) {
                        +" ${lesson[Lesson.Prompt]} / ${lesson[Lesson.Response]} "
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
    form(action = "/user/only/plan/$plan/lesson", method = FormMethod.post) {
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
        }
        p { submitInput { name = "add_lesson" } }
    }
}
