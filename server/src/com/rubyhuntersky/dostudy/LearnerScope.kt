package com.rubyhuntersky.dostudy

import com.rubyhuntersky.data.v2.Learner
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.Tomic

interface LearnerScope {
    val tomic: Tomic
    val learner: Peer<Learner.Name, String>
}

fun <T> learnerScope(learner: Peer<Learner.Name, String>, init: LearnerScope.() -> T): T {
    val scope = object : LearnerScope {
        override val tomic: Tomic = com.rubyhuntersky.tomic
        override val learner: Peer<Learner.Name, String> = learner
    }
    return scope.init()
}
