package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Minion
import kotlinx.html.*

fun HTML.renderStudy(
    addAssessmentAction: String,
    learner: Peer<Learner.Name, String>,
    study: Minion<Study.Owner>,
    assessments: Set<Minion<Assessment.Study>>
) = body {
    h6 { a(href = "/user/only") { +" ${learner[Learner.Name]}" } }
    form(action = "/user/only", method = FormMethod.post) {
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
        assessments.mapNotNull { assessment ->
            val productionResponse = assessment[Assessment.ProductionResponse]
            when {
                productionResponse != null -> {
                    li {
                        val prompt = assessment[Assessment.Prompt] ?: "No Prompt"
                        val level = assessment[Assessment.Level] ?: 0
                        +"($prompt) → $productionResponse [ L$level ]"
                    }
                }
                else -> null
            }
        }
    }
    form(action = addAssessmentAction, method = FormMethod.post) {
        h3 { +"Add Assessment" }
        ul {
            textInput(name = "prompt") {
                placeholder = "Prompt"
                required = true
            }
            +" → "
            textInput(name = "production_response") {
                placeholder = "Produce"
                required = true
            }
            +" [ "
            textInput(name = "level") {
                type = InputType.number
                min = "0"
                placeholder = "Level"
                required = true
            }
            +" ] "
            submitInput { value = "Add Production" }
        }
    }
}

fun HTML.renderPlan(learner: Peer<Learner.Name, String>, plan: Long) = body {
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

fun HTML.renderLearner(
    learner: Peer<Learner.Name, String>,
    database: Database
) = body {
    h1 { +" User / ${learner[Learner.Name]} " }

    form(action = "/user/only", method = FormMethod.post) {
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
                    +study.displayName
                    +" [ "
                    a(href = "${"/user/only/study"}/${study.ent}") { +"Edit" }
                    +" ] "
                }
            }
        }
    }

    h2 {
        form(action = "/user/only", method = FormMethod.post) {
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
            path = "/user/only/plan"
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