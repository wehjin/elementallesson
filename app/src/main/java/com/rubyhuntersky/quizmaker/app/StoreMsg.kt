package com.rubyhuntersky.quizmaker.app

import com.rubyhuntersky.data.Study
import kotlinx.coroutines.channels.SendChannel

sealed class StoreMsg {
    data class ReadStudy(val response: SendChannel<Study>) : StoreMsg()
    data class WriteStudy(val study: Study) : StoreMsg()
}