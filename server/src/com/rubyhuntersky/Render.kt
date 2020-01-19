package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.editstudy.listenUrl
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Minion
import kotlinx.html.*

const val userUrl = "/user/only"

fun HTML.renderSession(
    reportUrl: String,
    learner: Peer<Learner.Name, String>,
    study: Minion<Study.Owner>,
    assessmentList: List<Minion<Assessment.Study>>
) {
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
        h6 { a(userUrl) { +" ${learner[Learner.Name]}" } }
        h3 { +"${study[Study.Name].nullIfBlank() ?: "Untitled"} Session" }
        +"${assessmentList.size} remaining"

        val assessment = assessmentList.first()
        val producePrompt = assessment[Assessment.Prompt]
        if (producePrompt != null) {
            h1 { +producePrompt }
            renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ProductionResponse]}" }
        }
        val listenPrompt = assessment[Assessment.ListenPrompt]
        if (listenPrompt != null) {
            h1 {
                audio {
                    autoPlay = true
                    controls = true
                    autoBuffer = true
                    src = listenUrl(listenPrompt)
                }
            }
            renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ListenResponse]}" }
        }
        val clozePrompt = assessment[Assessment.ClozeTemplate]
        if (clozePrompt != null) {
            h1 { +clozePrompt }
            renderAnswerBlock(reportUrl) { +"${assessment[Assessment.ClozeFill]}" }
        }
    }
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
        p("obscured") {
            id = "reportP"
            select {
                name = "report_select"
                option {
                    value = "fail_assessment"
                    +"Failed it!\u2003Repeat test."
                }
                option {
                    value = "pass_assessment"
                    +"Nailed it.\u2003Rest then retest."
                }
            }
            +"\u2003"
            submitInput { value = "Next" }
        }
    }
}

fun HTML.renderPlan(learner: Peer<Learner.Name, String>, plan: Long) = body {
    h6 { a(userUrl) { +" ${learner[Learner.Name]}" } }
    form(userUrl, method = FormMethod.post) {
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
                    form("$userUrl/plan/$plan", method = FormMethod.post) {
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
    form("$userUrl/plan/$plan", method = FormMethod.post) {
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

fun HTML.renderLearner(
    learner: Peer<Learner.Name, String>,
    database: Database
) = body {
    h1 { +" User / ${learner[Learner.Name]} " }

    form(userUrl, method = FormMethod.post) {
        h2 {
            +" Studies "
            textInput { name = "add_study"; placeholder = "Name" }
            +" "
            submitInput { }
        }
    }
    ul {
        val studies = database.readStudies(learner.ent).sortedBy { it.displayName }
        if (studies.isEmpty()) +"None"
        else {
            studies.map<Minion<*>, Unit> { study ->
                li {
                    a("$userUrl/session/${study.ent}") { +study.displayName }
                    +" [ "
                    a("$userUrl/study/${study.ent}") { +"Edit" }
                    +" ] "
                }
            }
        }
    }

    h2 {
        form(action = userUrl, method = FormMethod.post) {
            +"Plans "
            textInput { name = "add_plan"; placeholder = "Name" }
            +" "
            submitInput { }
        }
    }
    ul {
        render(
            minions = tomic.readPlans(learner.ent),
            nameAttr = Plan.Name,
            path = "$userUrl/plan"
        )
    }
}

private val Minion<*>.displayName: String
    get() = this[Study.Name].nullIfBlank() ?: "Untitled"

private fun UL.render(minions: Set<Minion<*>>, nameAttr: Attribute2<String>, path: String) {
    if (minions.isEmpty()) +"None"
    else {
        minions.map { minion ->
            val displayName = minion[nameAttr].nullIfBlank() ?: "Untitled"
            li {
                a(href = "$path/${minion.ent}") { +" $displayName " }
            }
        }
    }
}

fun String?.nullIfBlank(): String? = if (this != null && this.isNotBlank()) this else null