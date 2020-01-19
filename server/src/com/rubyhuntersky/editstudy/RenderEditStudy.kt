package com.rubyhuntersky.editstudy

import com.rubyhuntersky.data.v2.Assessment
import com.rubyhuntersky.data.v2.Learner
import com.rubyhuntersky.data.v2.Study
import com.rubyhuntersky.nullIfBlank
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.userUrl
import kotlinx.html.*
import java.util.*

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

fun OL.renderAssessment(assessment: Minion<Assessment.Study>): Unit? {
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

fun listenUrl(prompt: String) =
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
