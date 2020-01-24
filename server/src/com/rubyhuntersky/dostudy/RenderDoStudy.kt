package com.rubyhuntersky.dostudy

import com.rubyhuntersky.nullIfBlank
import com.rubyhuntersky.userUrl
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
                +"$vision"
//                +"${assessmentList.size} remaining"
//
//                val assessment = assessmentList.first()
//                val producePrompt = assessment[Assessment.Prompt]
//                if (producePrompt != null) {
//                    h1 { +producePrompt }
//                    renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ProductionResponse]}" }
//                }
//                val listenPrompt = assessment[Assessment.ListenPrompt]
//                if (listenPrompt != null) {
//                    h1 {
//                        audio {
//                            autoPlay = true
//                            controls = true
//                            autoBuffer = true
//                            src = listenUrl(listenPrompt)
//                        }
//                    }
//                    renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ListenResponse]}" }
//                }
//                val clozePrompt = assessment[Assessment.ClozeTemplate]
//                if (clozePrompt != null) {
//                    h1 { +clozePrompt }
//                    renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ClozeFill]}" }
//                }
            }
        }
    }
}