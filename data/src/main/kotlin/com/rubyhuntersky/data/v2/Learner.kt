package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.*
import com.rubyhuntersky.tomedb.basics.Ent
import kotlin.math.absoluteValue
import kotlin.random.Random

object Learner : AttributeGroup {

    object Name : AttributeInObject<String>() {
        override val description: String = "The name of the learner."
        override val scriber: Scriber<String> = StringScriber
    }
}

fun Tomic.createLearner(
    name: String
): Peer<Learner.Name, String> = reformPeers(Learner.Name) {
    reforms = formPeer(name)
    peer(name)
}

object Plan : AttributeGroup {
    object Name : AttributeInObject<String>() {
        override val description: String = "The name of the plan."
        override val scriber: Scriber<String> = StringScriber
    }

    object Author : AttributeInObject<Ent>() {
        override val description: String = "The author of the plan."
        override val scriber: Scriber<Ent> = EntScriber
    }
}

fun Tomic.createPlan(author: Long, name: String): Minion<Plan.Author> =
    reformMinions(Leader(author, Plan.Author)) {
        val ent = Random.nextLong().absoluteValue
        reforms = formMinion(ent) { Plan.Name set name }
        minion(ent)
    }
