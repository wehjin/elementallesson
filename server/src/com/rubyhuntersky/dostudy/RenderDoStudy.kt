package com.rubyhuntersky.dostudy

import com.rubyhuntersky.editstudy.listenUrl
import com.rubyhuntersky.nullIfBlank
import com.rubyhuntersky.userUrl
import io.ktor.http.Parameters
import kotlinx.html.*

fun HTML.renderDoStudy(actionUrl: String, vision: DoStudyVision) {
    head {
        link {
            rel = "stylesheet"
            href = "/static/styles.css"
        }
        script {
            type = "text/javascript"
            src = "/static/session.js"
        }
    }
    body {
        h6 { a(userUrl) { +" ${vision.learnerName}" } }
        h3 { +"${vision.studyName.nullIfBlank() ?: "Untitled"} Session" }

        when (vision) {
            is ReadyToStudy -> {
                +"${vision.newCount} new, ${vision.awakeCount} active, ${vision.restCount} resting"
                p {
                    if ((vision.newCount + vision.awakeCount) > 0) {
                        form(action = actionUrl, method = FormMethod.post) {
                            hiddenInput { name = "actionType"; value = StartStudy::class.java.simpleName }
                            submitInput { value = "Start" }
                        }
                    } else {
                        i { +"No lessons are new or active right now.  Please try again later." }
                    }
                }
            }
            is Studying -> {
                +"${vision.todo.size + 1} remaining"
                when (val assessmentVision = vision.assessmentVision) {
                    is ProduceVision -> {
                        h1 { +assessmentVision.prompt }
                        renderAnswerBlock(actionUrl) { +assessmentVision.response }
                    }
                    is ListenVision -> {
                        h1 {
                            audio {
                                autoPlay = true
                                controls = true
                                autoBuffer = true
                                src = listenUrl(assessmentVision.search)
                            }
                        }
                        renderAnswerBlock(actionUrl) { +assessmentVision.response }
                    }
                    is ClozeVision -> {
                        h1 { +assessmentVision.template }
                        renderAnswerBlock(actionUrl) { +assessmentVision.fill }
                    }
                }
                p { +"$vision" }
            }
        }
    }
}

val Parameters.doStudyAction: DoStudyAction
    get() = when (this["actionType"]) {
        StartStudy::class.java.simpleName -> StartStudy()
        RecordAssessment::class.java.simpleName -> RecordAssessment(this["passedFailed"] == "passed")
        else -> error("No actionType in parameters: ${this}")
    }

private fun BODY.renderAnswerBlock(reportUrl: String, answerBlock: H1.() -> Unit) {
    button {
        id = "answerButton"
        onClick = "revealAnswer()"
        +"Check Answer"
    }
    h1("obscured") {
        id = "answerP"
        +"→\u2002"
        run(answerBlock)
    }
    form(reportUrl, method = FormMethod.post) {
        hiddenInput { name = "actionType"; value = RecordAssessment::class.java.simpleName }
        p("obscured") {
            id = "reportP"
            select {
                name = "passedFailed"
                option {
                    value = "failed"
                    +"Failed it!\u2003Repeat test."
                }
                option {
                    value = "passed"
                    +"Nailed it.\u2003Rest then retest."
                }
            }
            +"\u2003"
            submitInput { value = "Next" }
        }
    }
}

