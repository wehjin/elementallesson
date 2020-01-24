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
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.util.pipeline.PipelineContext
import kotlinx.html.body
import kotlinx.html.form
import kotlinx.html.submitInput
import java.io.File
import java.util.*
import kotlin.time.ExperimentalTime

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

private val homeDir = System.getenv("HOME")
@Suppress("SpellCheckingInspection")
private val appDir = File(homeDir, ".studycatastrophe")
private val tomeDir = File(appDir, "tome")
val tomic = tomicOf(tomeDir) { emptyList() }

interface StudyNumberScope {
    val studyNumber: Long
}

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
                fun PipelineContext<Unit, ApplicationCall>.doStudyVision(): DoStudyVision {
                    val studyNumber = call.parameters["study"]?.toLongOrNull() ?: error("Unspecified study")
                    return doStudyScope(learner) { initDoStudy(studyNumber, Date()) }
                }
                get {
                    val vision = doStudyVision()
                    call.respondHtml { renderDoStudy(call.request.uri, vision) }
                }
                post {
                    val vision = doStudyVision()
                    val params = call.receive<Parameters>()
                    val action = when (params["actionType"]) {
                        StartStudy::class.java.simpleName -> StartStudy()
                        else -> error("No actionType in parameters: ${call.parameters}")
                    }
                    val newVision = doStudyScope(learner) { updateDoStudy(vision, action) }
                    call.respondText("$newVision")
                    //call.respondRedirect(call.request.uri)
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

