package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.EntScriber
import com.rubyhuntersky.tomedb.attributes.StringScriber
import com.rubyhuntersky.tomedb.basics.Ent
import kotlin.math.absoluteValue
import kotlin.random.Random

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


fun Tomic.deleteStudy(
    study: Minion<Study.Owner>
) = this.reformMinions(study.leader) {
    reforms = study.unform
    minions
}

fun Tomic.updateStudy(
    study: Minion<Study.Owner>,
    collectReforms: EntReformScope.() -> Unit
) = this.reformMinions(study.leader) {
    this.reforms = study.reform(collectReforms)
    minions
}

fun Tomic.readStudies(owner: Long): Set<Minion<Study.Owner>> = this.minions(Leader(owner, Study.Owner))

fun Tomic.createStudy(
    owner: Long,
    name: String
) = reformMinions(Leader(owner, Study.Owner)) {
    val ent = Random.nextLong().absoluteValue
    reforms = formMinion(ent) { Study.Name set name }
    minion(ent)
}
