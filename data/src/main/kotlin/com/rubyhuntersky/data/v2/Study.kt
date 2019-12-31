package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.EntScriber
import com.rubyhuntersky.tomedb.attributes.StringScriber
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.minion.*

object Study : AttributeGroup {
    object Name : AttributeInObject<String>() {
        override val description = "The name of the study"
        override val scriber = StringScriber
    }

    object Owner : AttributeInObject<Ent>() {
        override val description = "The owner of the study"
        override val scriber = EntScriber
    }
}

fun Tomic.deleteStudy(owner: Long, study: Long): Minion<Study.Owner>? {
    return unformMinion(Leader(owner, Study.Owner), study)
}

fun Tomic.updateStudy(
    study: Minion<Study.Owner>,
    collectReforms: MinionReformScope<Study.Owner>.() -> Unit
): Set<Minion<Study.Owner>> {
    reformMinion(study.leader, study.ent, collectReforms)
    return latest.minions(study.leader)
}

fun Tomic.readStudies(owner: Long): Set<Minion<Study.Owner>> {
    return this.latest.minions(Leader(owner, Study.Owner))
}

fun Tomic.createStudy(owner: Long, name: String): Minion<Study.Owner> {
    val leader = Leader(owner, Study.Owner)
    return formMinion(leader) { Study.Name set name }
}
