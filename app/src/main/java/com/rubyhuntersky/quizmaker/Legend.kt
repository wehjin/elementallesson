package com.rubyhuntersky.quizmaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class Legend<MdlT : Any, MsgT : Any> : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job()
    private val mdlBroadcast = ConflatedBroadcastChannel<MdlT>()
    private val msgChannel = Channel<MsgT>()
    private lateinit var job: Job

    fun toMdls() = mdlBroadcast.openSubscription()

    suspend fun send(msg: MsgT) = msgChannel.send(msg)
    fun offer(msg: MsgT) = msgChannel.offer(msg)

    internal fun run(block: suspend (mdls: SendChannel<MdlT>, msgs: Channel<MsgT>) -> Unit) {
        job = launch { block(mdlBroadcast, msgChannel) }
    }
}


@ExperimentalCoroutinesApi
fun <MdlT : Any, MsgT : Any> legendOf(
    block: suspend (mdls: SendChannel<MdlT>, msgs: Channel<MsgT>) -> Unit
) = Legend<MdlT, MsgT>().apply { run(block) }