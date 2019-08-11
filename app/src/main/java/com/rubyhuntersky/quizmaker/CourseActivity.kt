package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
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

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRenderer(legend)
    }

    override fun onBackPressed() {
        launch { legend.msgs.send(ViewCourseMsg.Quit) }
        super.onBackPressed()
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
            val models = legend.mdls.openSubscription()
            val events = Channel<String>()
            while (!isDestroyed && !models.isClosedForReceive && !events.isClosedForReceive) {
                select<Unit> {
                    models.onReceive { mdl ->
                        renderCourse(mdl.course, events)
                    }
                    events.onReceive { event ->
                        when (event) {
                            "continueCourse" -> Unit
                            "cancelCourse" -> legend.msgs.send(ViewCourseMsg.Quit)
                        }
                    }
                }
            }
            events.close()
            models.cancel()
            GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
                ?.finishGuidedStepSupportFragments()
        }
    }

    private suspend fun renderCourse(course: Course, events: Channel<String>) {
        val guidance = GuidanceStylist.Guidance(
            course.title,
            course.subtitle ?: "",
            "Lessons: ${course.getActiveLessons(LocalDateTime.now()).size}",
            getDrawable(R.drawable.ic_launcher_background)
        )
        val buttons = listOf(
            StepFragment.Button("Start", event = "continueCourse", hasNext = true),
            StepFragment.Button("Cancel", event = "cancelCourse", hasNext = false)
        )
        courseFragment.control.send(StepFragment.Msg.SetView(guidance, buttons, events))
    }

    private val courseFragment: StepFragment
        get() = (GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager) as? StepFragment)
            ?: StepFragment().also {
                GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content)
            }
}
