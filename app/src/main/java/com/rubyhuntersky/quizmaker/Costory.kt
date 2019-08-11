package com.rubyhuntersky.quizmaker

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@ExperimentalCoroutinesApi
data class Costory<MdlT : Any, MsgT : Any>(
    val mdls: ConflatedBroadcastChannel<MdlT>,
    val msgs: Channel<MsgT>
)