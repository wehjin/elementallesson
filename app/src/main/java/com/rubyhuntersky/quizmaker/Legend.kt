package com.rubyhuntersky.quizmaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class Legend<MdlT : Any, MsgT : Any> {

    private val mdlBroadcast = ConflatedBroadcastChannel<MdlT>()
    private val msgChannel = Channel<MsgT>()
    private lateinit var job: Job

    fun run(scope: CoroutineScope, block: suspend (mdls: SendChannel<MdlT>, msgs: Channel<MsgT>) -> Unit) {
        job = scope.launch { block(mdlBroadcast, msgChannel) }
    }

    fun startMdls() = mdlBroadcast.openSubscription()

    fun offer(msg: MsgT) = msgChannel.offer(msg)
    suspend fun send(msg: MsgT) = msgChannel.send(msg)

    fun cancel() {
        msgChannel.close()
        job.cancel()
    }
}


@Suppress("EXPERIMENTAL_API_USAGE")
interface LegendScope : CoroutineScope

@ExperimentalCoroutinesApi
inline fun <reified MdlT : Any, reified MsgT : Any> LegendScope.startLegend(
    name: String = "unit",
    noinline block: suspend (mdls: SendChannel<MdlT>, msgs: Channel<MsgT>) -> MdlT
): Legend<MdlT, MsgT> {
    val key = name.toNamedLegendKey<MdlT, MsgT>()
    namedLegends[key]?.cancel()
    return Legend<MdlT, MsgT>().also { legend ->
        namedLegends[key] = legend
        legend.run(this) { mdls, msgs ->
            block(mdls, msgs)
            namedLegends.remove(key, legend)
        }
    }
}

inline fun <reified MdlT : Any, reified MsgT : Any> String.toNamedLegendKey(): Triple<String, Class<MdlT>, Class<MsgT>> =
    Triple(this, MdlT::class.java, MsgT::class.java)

@ExperimentalCoroutinesApi
val namedLegends = mutableMapOf<Triple<String, Class<*>, Class<*>>, Legend<*, *>>()

@ExperimentalCoroutinesApi
inline fun <reified MdlT : Any, reified MsgT : Any> LegendScope.findLegend(name: String = "unit"): Legend<MdlT, MsgT>? {
    val key = name.toNamedLegendKey<MdlT, MsgT>()
    @Suppress("UNCHECKED_CAST")
    return namedLegends[key] as? Legend<MdlT, MsgT>
}
