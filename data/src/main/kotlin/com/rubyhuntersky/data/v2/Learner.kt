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

private const val onlyLearnerName = "only-learner"

fun Tomic.createLearner(): Peer<Learner.Name, String> = reformPeers(Learner.Name) {
    val learner = peerOrNull(onlyLearnerName)
    if (learner == null) {
        reforms = formPeer(onlyLearnerName)
    }
    peer(onlyLearnerName)
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

fun Tomic.createPlan(
    author: Long,
    name: String
): Minion<Plan.Author> = reformMinions(Leader(author, Plan.Author)) {
    val ent = Random.nextLong().absoluteValue
    reforms = formMinion(ent) { Plan.Name set name }
    minion(ent)
}

fun Tomic.readPlans(
    author: Long
): Set<Minion<Plan.Author>> = minions(Leader(author, Plan.Author))

fun Tomic.updatePlans(
    author: Long,
    init: MinionMob<Plan.Author>.() -> List<Form<*>>
): Set<Minion<Plan.Author>> = reformMinions(Leader(author, Plan.Author)) {
    reforms = this.init()
    minions
}

fun Tomic.deletePlan(
    author: Long,
    plan: Long
): Set<Minion<Plan.Author>> = reformMinions(Leader(author, Plan.Author)) {
    reforms = minion(plan).unform
    minions
}
