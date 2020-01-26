package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.dostudy.*
import com.rubyhuntersky.editstudy.editStudyAction
import com.rubyhuntersky.editstudy.renderStudy
import com.rubyhuntersky.editstudy.updateEditStudyModel
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.tomedb.tomicOf
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.Parameters
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.sessions.*
import kotlinx.coroutines.channels.Channel
import kotlinx.html.body
import kotlinx.html.form
import kotlinx.html.submitInput
import java.io.File
import kotlin.time.ExperimentalTime

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

data class StudySession(
    val storyNumber: Int
)

private val homeDir = System.getenv("HOME")
@Suppress("SpellCheckingInspection")
private val appDir = File(homeDir, ".studycatastrophe")
private val tomeDir = File(appDir, "tome")
val tomic = tomicOf(tomeDir) { emptyList() }

val doStudyStories = mutableMapOf<Int, DoStudyStory>()

@ExperimentalTime
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

    install(Sessions) {
        cookie<StudySession>("STUDY_SESSION_ID", SessionStorageMemory()) {
            cookie.path = "/"
        }
    }

    routing {
        static("/static") {
            resources("static")
        }

        get("/") { call.respondRedirect("/start") }

        route("start") {
            get {
                call.respondHtml {
                    body {
                        form(action = "/user/only") { submitInput { value = "Sign In" } }
                    }
                }
            }
        }

        route("user/{user}") {
            val learner = tomic.createLearner()
            val learnerUrl = "/user/${learner.ent}"
            get {
                call.respondHtml { renderLearner(learner, tomic.latest) }
            }
            post {
                val params: Parameters = call.receive()
                log.trace("Parameters: $params")
                updateLearner(learner, params)
                call.respondRedirect("/user/only")
            }
            route("session/{study}") {
                get {
                    val render = Channel<DoStudyVision?>(1)
                    val sessionStory = call.sessions.get<StudySession>()?.let { doStudyStories[it.storyNumber] }
                    val story = if (sessionStory == null) {
                        val newStory = doStudy(
                            studyNumber = call.parameters["study"]?.toLongOrNull() ?: error("Unspecified study"),
                            learner = learner,
                            firstRender = render
                        )
                        newStory.also {
                            doStudyStories[newStory.number] = it
                            call.sessions.set(StudySession(it.number))
                        }
                    } else {
                        sessionStory.also {
                            it.messages.send(ActionRender(null, render))
                        }
                    }
                    render.receive()?.let { vision ->
                        call.respondHtml { renderDoStudy(call.request.uri, vision) }
                    } ?: call.respondRedirect(learnerUrl).also { doStudyStories.remove(story.number) }
                }
                post {
                    val story = call.sessions.get<StudySession>()?.let { doStudyStories[it.storyNumber] }
                    story?.let {
                        story.messages.send(ActionRender(call.receive<Parameters>().doStudyAction, null))
                        call.respondRedirect(call.request.uri)
                    } ?: call.respondRedirect(learnerUrl)
                }
            }
            route("study/{study}") {
                get {
                    val db = tomic.latest
                    val study = db.readStudy(call, learner.ent)
                    val assessments = db.readAssessments(study)
                    this.call.respondHtml { renderStudy(call.request.uri, learner, study, assessments) }
                }
                post {
                    val action = editStudyAction(call, learner.ent)
                    action?.let { updateEditStudyModel(tomic, action) }
                    call.respondRedirect(call.request.uri)
                }
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
        }
    }
}

private fun Database.readStudy(call: ApplicationCall, learnerNumber: Long): Minion<Study.Owner> {
    return readStudy(call.parameters["study"]?.toLongOrNull(), learnerNumber)
        ?: error("Invalid study parameter ${call.parameters["study"]}")
}

private fun updateLearner(learner: Peer<Learner.Name, String>, params: Parameters) {
    params["add_plan"]?.let { tomic.createPlan(learner.ent, it) }
    params["drop_plan"]?.toLongOrNull()?.let { tomic.deletePlan(learner.ent, it) }
    params["add_study"]?.let { tomic.createStudy(it, learner.ent) }
    params["drop_study"]?.toLongOrNull()?.let { tomic.deleteStudy(it, learner.ent) }
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

