package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Form
import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.*
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.minion.*
import kotlin.math.absoluteValue
import kotlin.random.Random

object Lesson : AttributeGroup {
    object Prompt : AttributeInObject<String>() {
        override val description: String = "The lesson prompt."
        override val scriber: Scriber<String> = StringScriber
    }

    object PromptColoring : AttributeInObject<String>() {
        override val description: String = "Coloring for the lesson prompt."
        override val scriber: Scriber<String> = StringScriber
    }

    object Response : AttributeInObject<String>() {
        override val description: String = "The lesson response."
        override val scriber: Scriber<String> = StringScriber
    }

    object ResponseColoring : AttributeInObject<String>() {
        override val description: String = "Coloring for the lesson response."
        override val scriber: Scriber<String> = StringScriber
    }

    object Plan : AttributeInObject<Ent>() {
        override val description: String = "The plan holding the lesson."
        override val scriber: Scriber<Ent> = EntScriber
    }

    object Level : AttributeInObject<Long>() {
        override val description: String = "The level of the lesson"
        override val scriber: Scriber<Long> = LongScriber
    }
}

fun Tomic.createPlanLesson(
    plan: Long,
    prompt: String,
    response: String,
    level: Long,
    responseColoring: String? = null,
    promptColoring: String? = null
): Minion<Lesson.Plan> = reformMinions(Leader(plan, Lesson.Plan)) {
    val ent = Random.nextLong().absoluteValue
    reforms = formMinion(ent) {
        Lesson.Prompt set prompt
        Lesson.Response set response
        Lesson.Level set level
        promptColoring?.also { Lesson.PromptColoring set it }
        responseColoring?.also { Lesson.ResponseColoring set it }
    }
    minion(ent)
}

fun Tomic.readPlanLessons(
    plan: Long
): Set<Minion<Lesson.Plan>> = latest.minions(Leader(plan, Lesson.Plan))

fun Tomic.updatePlanLessons(
    plan: Long,
    init: MinionMob<Lesson.Plan>.() -> List<Form<*>>
): Set<Minion<Lesson.Plan>> = reformMinions(Leader(plan, Lesson.Plan)) {
    reforms = this.init()
    minions
}

fun Tomic.deletePlanLesson(
    plan: Long,
    lesson: Long
): Set<Minion<Lesson.Plan>> {
    val leader = Leader(plan, Lesson.Plan)
    unformMinion(leader, lesson)
    return latest.minions(leader)
}
