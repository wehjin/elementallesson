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

fun Tomic.addLearner(
    badge: String
): Peer<Learner.Name, String> = reformPeers(Learner.Name) {
    reforms = formPeer(badge)
    peer(badge)
}
