package com.rubyhuntersky.quizmaker

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

@ExperimentalCoroutinesApi
object ChannelLookup {

    operator fun get(id: Long): Channel<Any>? = map[id]

    private val map = mutableMapOf<Long, Channel<Any>>()

    operator fun set(id: Long, channel: Channel<Any>?) {
        if (channel == null) {
            map.remove(id)
        } else {
            map[id] = channel
        }
    }
}