package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.Scriber
import com.rubyhuntersky.tomedb.attributes.StringScriber
import com.rubyhuntersky.tomedb.reformPeers

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
