package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.*
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.*
import java.util.*

object Assessment : AttributeGroup {

    object PassCount : AttributeInObject<Long>() {
        override val description: String = "The number of times the assessment was passed"
        override val scriber: Scriber<Long> = LongScriber
    }

    object RunTime : AttributeInObject<Date>() {
        override val description: String = "A recent time at which the assessment was run"
        override val scriber: Scriber<Date> = DateScriber
    }

    object Study : AttributeInObject<Ent>() {
        override val description: String = "Each assessment belongs to a study."
        override val scriber: Scriber<Ent> = EntScriber
    }

    object Creation : AttributeInObject<Date>() {
        override val description: String = "The time the assessment was created"
        override val scriber: Scriber<Date> = DateScriber
    }

    object ClozeFill : AttributeInObject<String>() {
        override val description: String = "The fill part of a cloze assessment"
        override val scriber: Scriber<String> = StringScriber
    }

    object ClozeTemplate : AttributeInObject<String>() {
        override val description: String = "The template part of a cloze assessment"
        override val scriber: Scriber<String> = StringScriber
    }

    object ListenResponse : AttributeInObject<String>() {
        override val description: String = "The learner should produce this response to pass a listen assessment."
        override val scriber: Scriber<String> = StringScriber
    }

    object ListenPrompt : AttributeInObject<String>() {
        override val description: String = "Describes the audio clip to play when starting a listen assessment."
        override val scriber: Scriber<String> = StringScriber
    }

    object ProductionResponse : AttributeInObject<String>() {
        override val description: String = "The learner must produce this response to pass an assessment."
        override val scriber: Scriber<String> =
            StringScriber
    }

    object Prompt : AttributeInObject<String>() {
        override val description: String = "The learner sees this at the start of an assessment."
        override val scriber: Scriber<String> =
            StringScriber
    }

    object Level : AttributeInObject<Long>() {
        override val description: String = "The level of the assessment"
        override val scriber: Scriber<Long> = LongScriber
    }
}

fun Database.createAudit(study: Minion<Study.Owner>): List<Minion<Assessment.Study>> {
    return visitMinions(Leader(study.ent, Assessment.Study)) {
        val untested = minionList.filter { 0L == it[Assessment.PassCount] ?: 0L }
        untested.shuffled().take(5)
    }
}

fun Tomic.createClozeAssessment(study: Minion<Study.Owner>, fill: String, template: String, level: Long) {
    reformMinions(Leader(study.ent, Assessment.Study)) {
        reforms = formMinion {
            Assessment.ClozeFill set fill
            Assessment.ClozeTemplate set template
            Assessment.Level set level
            Assessment.Creation set Date()
        }
    }
}

fun Tomic.createListenAssessment(study: Minion<Study.Owner>, response: String, prompt: String, level: Long) {
    reformMinions(Leader(study.ent, Assessment.Study)) {
        reforms = formMinion {
            Assessment.ListenResponse set response
            Assessment.ListenPrompt set prompt
            Assessment.Level set level
            Assessment.Creation set Date()
        }
    }
}

fun Tomic.createProductionAssessment(study: Minion<Study.Owner>, response: String, prompt: String, level: Long) {
    reformMinions(Leader(study.ent, Assessment.Study)) {
        reforms = formMinion {
            Assessment.ProductionResponse set response
            Assessment.Prompt set prompt
            Assessment.Level set level
            Assessment.Creation set Date()
        }
    }
}

fun Database.readAssessments(study: Minion<Study.Owner>): Set<Minion<Assessment.Study>> {
    return minions(Leader(study.ent, Assessment.Study))
}
