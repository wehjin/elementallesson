package com.rubyhuntersky.quizmaker.app

import android.app.Application
import com.rubyhuntersky.data.Study
import com.rubyhuntersky.data.material.BasicDegreeMaterial
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.viewcourse.startViewCourseLegend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
class App : Application(), CoroutineScope, LegendScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = job
    private val storeChannel = Channel<StoreMsg>(10)

    init {
        val appLegend = startAppLegend(storeCtl)
        startViewCourseLegend(appLegend)
    }

    private val storeCtl
        get() = storeChannel as SendChannel<StoreMsg>

    override fun onCreate() {
        super.onCreate()
        launch {
            val studyFile = File(filesDir, "activeStudy")
            var study = Study.start(BasicDegreeMaterial, LocalDateTime.now()).mergeInto(StudyStore.read(studyFile))
            while (!storeChannel.isClosedForReceive) {
                when (val msg = storeChannel.receive()) {
                    is StoreMsg.ReadStudy -> msg.response.send(study)
                    is StoreMsg.WriteStudy -> {
                        study = msg.study.also { StudyStore.write(it, studyFile) }
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        job.cancel()
        super.onTerminate()
    }
}