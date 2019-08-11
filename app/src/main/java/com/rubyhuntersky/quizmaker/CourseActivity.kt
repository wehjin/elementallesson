package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.chapter10CourseMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CourseActivity : FragmentActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    private val legend = launchLegend()
    private val evts = Channel<Any>()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRenderer(legend)
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.fragments.forEach {
            if (it is FirstStepFragment) it.evts = evts
        }
    }

    override fun onBackPressed() {
        launch { legend.msgs.send(ViewCourseMsg.Quit) }
        super.onBackPressed()
    }

    override fun onStop() {
        supportFragmentManager.fragments.forEach {
            if (it is FirstStepFragment) it.evts = null
        }
        super.onStop()
    }

    override fun onDestroy() {
        evts.close()
        super.onDestroy()
    }

    private fun launchLegend(): Legend<ViewCourseMdl, ViewCourseMsg> {
        val legend = Legend(ConflatedBroadcastChannel<ViewCourseMdl>(), Channel<ViewCourseMsg>())
        launch {
            var mdl = ViewCourseMdl(Course.start(chapter10CourseMaterial, LocalDateTime.now()))
                .also { legend.mdls.send(it) }
            for (msg in legend.msgs) {
                mdl = when (msg) {
                    is ViewCourseMsg.Quit -> mdl.also { legend.msgs.close() }
                }.also { legend.mdls.send(it) }
            }
            legend.mdls.close()
            finish()
        }
        return legend
    }

    private fun launchRenderer(legend: Legend<ViewCourseMdl, ViewCourseMsg>) {
        launch {
            val mdls = legend.mdls.openSubscription()
            while (!isDestroyed && !mdls.isClosedForReceive && !evts.isClosedForReceive) {
                select<Unit> {
                    evts.onReceive { action ->
                        if (1L == action) {
                            legend.msgs.send(ViewCourseMsg.Quit)
                        }
                    }
                    mdls.onReceive { mdl ->
                        val fragment = FirstStepFragment.build(
                            title = mdl.course.title,
                            subtitle = mdl.course.subtitle,
                            count = mdl.course.getActiveLessons(LocalDateTime.now()).size
                        ).also { it.evts = evts }
                        GuidedStepSupportFragment.add(supportFragmentManager, fragment, android.R.id.content)
                    }
                }
            }
            evts.close()
            GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
                ?.finishGuidedStepSupportFragments()
        }
    }

}
