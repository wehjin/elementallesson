package com.rubyhuntersky

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.Minion
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.get
import kotlinx.html.*


fun HTML.renderStudy(learner: Peer<Learner.Name, String>, study: Minion<Study.Owner>) = body {
    val studyName = study[Study.Name].nullIfBlank() ?: "Untitled"
    h6 { a(href = "/user/only") { +" ${learner[Learner.Name]}" } }
    form(action = "/user/only", method = FormMethod.post) {
        +"[ Study / ${study.ent.toString(16)} ]"
        h1 {
            +"$studyName "
            hiddenInput {
                name = "drop_study"
                value = study.ent.toString()
            }
            submitInput { value = "Drop" }
        }
    }
    h2 { +"Lessons" }
    ol {
        form {
            textInput(name = "produce") {
                placeholder = "Produce"
                required = true
            }
            +" "
            textInput(name = "prompt") {
                placeholder = "Prompt"
                required = true
            }
            +" "
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

fun HTML.renderLearner(learner: Peer<Learner.Name, String>) = body {
    h1 { +" User / ${learner[Learner.Name]} " }

    form(action = "/user/only", method = FormMethod.post) {
        h2 {
            +" Studies "
            textInput { name = "add_study"; placeholder = "Name" }
            +" "
            submitInput { }
        }
    }
    ul { render(tomic.readStudies(learner.ent), Study.Name, "/user/only/study") }

    h2 {
        form(action = "/user/only", method = FormMethod.post) {
            +"Plans "
            textInput { name = "add_plan"; placeholder = "Name" }
            +" "
            submitInput { }
        }
    }
    ul { render(tomic.readPlans(learner.ent), Plan.Name, "/user/only/plan") }
}

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