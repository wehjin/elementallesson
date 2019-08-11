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
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CourseActivity : FragmentActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    private val legend = legendOf<ViewCourseMdl, ViewCourseMsg> { mdls, msgs ->
        var mdl = ViewCourseMdl(Course.start(chapter10CourseMaterial, LocalDateTime.now()))
        mdls.send(mdl)
        for (msg in msgs) {
            mdl = when (msg) {
                is ViewCourseMsg.Quit -> mdl.also { msgs.close() }
            }
            mdls.send(mdl)
        }
        mdls.close()
        finish()
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRenderer(legend)
    }

    override fun onBackPressed() {
        launch { legend.send(ViewCourseMsg.Quit) }
        super.onBackPressed()
    }

    private fun launchRenderer(legend: Legend<ViewCourseMdl, ViewCourseMsg>) {
        launch {
            val legendMdls = legend.toMdls()
            val viewEvts = Channel<String>()
            while (!isDestroyed && !legendMdls.isClosedForReceive && !viewEvts.isClosedForReceive) {
                select<Unit> {
                    legendMdls.onReceive { mdl ->
                        val course = mdl.course
                        renderCourse(course, viewEvts)
                    }
                    viewEvts.onReceive { event ->
                        when (event) {
                            "continueCourse" -> Unit
                            "cancelCourse" -> legend.send(ViewCourseMsg.Quit)
                        }
                    }
                }
            }
            stopRendering()
            viewEvts.close()
            legendMdls.cancel()
        }
    }

    private fun stopRendering() {
        GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
            ?.finishGuidedStepSupportFragments()
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
