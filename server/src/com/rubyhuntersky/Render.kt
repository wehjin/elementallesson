package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Minion
import kotlinx.html.*
import java.util.*

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

fun HTML.renderStudy(
    addAssessmentAction: String,
    learner: Peer<Learner.Name, String>,
    study: Minion<Study.Owner>,
    assessments: Set<Minion<Assessment.Study>>
) = body {
    h6 { a(userUrl) { +" ${learner[Learner.Name]}" } }
    form(userUrl, method = FormMethod.post) {
        +"[ Study / ${study.ent.toString(16)} ]"
        h1 {
            +"${study[Study.Name].nullIfBlank() ?: "Untitled"} "
            hiddenInput {
                name = "drop_study"
                value = study.ent.toString()
            }
            submitInput { value = "Drop" }
        }
    }
    h2 { +"Assessments" }
    ol {
        val past = Date(1000)
        assessments.sortedBy { it[Assessment.Creation] ?: past }.mapNotNull { renderAssessment(it) }
    }
    h3 { +"Add Assessment" }
    ul {
        li { renderAddProduction(addAssessmentAction) }
        li { renderAddListen(addAssessmentAction) }
        li { renderAddCloze(addAssessmentAction) }
    }
}

private fun OL.renderAssessment(assessment: Minion<Assessment.Study>): Unit? {
    val level = assessment[Assessment.Level] ?: 0
    val productionResponse = assessment[Assessment.ProductionResponse]
    val listenResponse = assessment[Assessment.ListenResponse]
    val clozeFill = assessment[Assessment.ClozeFill]
    return when {
        productionResponse != null -> {
            li {
                val prompt = assessment[Assessment.Prompt] ?: "No Prompt"
                +"[ L$level ] $prompt → $productionResponse"
            }
        }
        listenResponse != null -> {
            li {
                val prompt = assessment[Assessment.ListenPrompt] ?: "何？"
                +"[ L$level ] "
                a {
                    href = listenUrl(prompt)
                    +prompt
                }
                +" → $listenResponse"
            }
        }
        clozeFill != null -> {
            li {
                val prompt = assessment[Assessment.ClozeTemplate] ?: "{..}".replace(pseudoEllipsis, "…")
                +"[ L$level ] $prompt → $clozeFill"
            }
        }
        else -> null
    }
}

private fun listenUrl(prompt: String) =
    "http://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&q=${prompt}&tl=ja"

val pseudoEllipsis = Regex("[^.][.][.][^.]")


private fun LI.renderAddCloze(addAssessmentAction: String) {
    form(action = addAssessmentAction, method = FormMethod.post) {

        +" ["
        textInput(name = "cloze_level") {
            type = InputType.number
            min = "0"
            placeholder = "Level"
            required = true
        }
        +"] "
        textInput(name = "cloze_template") {
            placeholder = "Cloze"
            required = true
        }
        +" → "
        textInput(name = "cloze_fill") {
            placeholder = "Fill"
            required = true
        }
        +" "
        submitInput { value = "Add Cloze" }
    }
}

private fun LI.renderAddListen(addAssessmentAction: String) {
    form(action = addAssessmentAction, method = FormMethod.post) {

        +" ["
        textInput(name = "listen_level") {
            type = InputType.number
            min = "0"
            placeholder = "Level"
            required = true
        }
        +"] "
        textInput(name = "listen_prompt") {
            placeholder = "Listen"
            required = true
        }
        +" → "
        textInput(name = "listen_response") {
            placeholder = "Meaning"
            required = true
        }
        +" "
        submitInput { value = "Add Listen" }
    }
}

private fun LI.renderAddProduction(addAssessmentAction: String) {
    form(action = addAssessmentAction, method = FormMethod.post) {
        +" ["
        textInput(name = "level") {
            type = InputType.number
            min = "0"
            placeholder = "Level"
            required = true
        }
        +"] "
        textInput(name = "prompt") {
            placeholder = "Prompt"
            required = true
        }
        +" → "
        textInput(name = "production_response") {
            placeholder = "Produce"
            required = true
        }
        +" "
        submitInput { value = "Add Production" }
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

private fun String?.nullIfBlank(): String? = if (this != null && this.isNotBlank()) this else null