package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.*
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.minion.Leader
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.tomedb.minion.minions
import com.rubyhuntersky.tomedb.minion.reformMinions

object Assessment : AttributeGroup {
    object Study : AttributeInObject<Ent>() {
        override val description: String = "Each assessment belongs to a study."
        override val scriber: Scriber<Ent> =
            EntScriber
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
}

fun Tomic.createProductionAssessment(study: Minion<Study.Owner>, response: String, prompt: String) {
    reformMinions(Leader(study.ent, Assessment.Study)) {
        reforms = formMinion {
            Assessment.ProductionResponse set response
            Assessment.Prompt set prompt
        }
    }
}

fun Database.readAssessments(study: Minion<Study.Owner>): Set<Minion<Assessment.Study>> {
    return minions(Leader(study.ent, Assessment.Study))
}
