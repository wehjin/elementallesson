package com.rubyhuntersky.quizmaker.app

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.rubyhuntersky.data.Study
import com.rubyhuntersky.mepl.Mepl
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.tools.MaterialLoader
import com.rubyhuntersky.quizmaker.viewcourse.startViewCourseLegend
import io.fabric.sdk.android.Fabric
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
        Fabric.with(Fabric.Builder(this).kits(Crashlytics()).debuggable(true).build())
        Mepl.start(this)
        startStudyStore()
    }

    private fun startStudyStore() {
        launch {
            try {
                val studyFile = File(filesDir, "activeStudy")
                val degreeMaterial = MaterialLoader.basicDegreeMaterial
                var study = Study.start(degreeMaterial, LocalDateTime.now())
                    .mergeInto(StudyStore.read(studyFile))
                for (msg in storeChannel) {
                    when (msg) {
                        is StoreMsg.ReadStudy -> msg.response.send(study)
                        is StoreMsg.WriteStudy -> {
                            study = msg.study.also { StudyStore.write(it, studyFile) }
                        }
                    }
                }
            } catch (thrown: Throwable) {
                Log.e("StudyStore", "Failed to open", thrown)
                Crashlytics.logException(thrown)
            }
        }
    }

    override fun onTerminate() {
        job.cancel()
        Mepl.stop()
        super.onTerminate()
    }
}