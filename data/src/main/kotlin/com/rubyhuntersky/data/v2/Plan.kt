package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.EntScriber
import com.rubyhuntersky.tomedb.attributes.StringScriber
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.minion.*

object Plan : AttributeGroup {
    object Name : AttributeInObject<String>() {
        override val description = "The name of the plan."
        override val scriber = StringScriber

    }

    object Author : AttributeInObject<Ent>() {
        override val description = "The author of the plan."
        override val scriber = EntScriber
    }
}

fun Tomic.createPlan(author: Long, name: String): Minion<Plan.Author> {
    val leader = Leader(author, Plan.Author)
    return formMinion(leader) { Plan.Name set name }
}

fun Tomic.readPlans(author: Long): Set<Minion<Plan.Author>> {
    return latest.minions(Leader(author, Plan.Author))
}

fun Tomic.updatePlan(
    author: Long,
    plan: Long,
    reform: MinionReformScope<Plan.Author>.() -> Unit
): Set<Minion<Plan.Author>> {
    val leader = Leader(author, Plan.Author)
    reformMinion(leader, plan, reform)
    return latest.minions(leader)
}

fun Tomic.deletePlan(author: Long, plan: Long): Minion<Plan.Author>? {
    return unformMinion(Leader(author, Plan.Author), plan)
}
