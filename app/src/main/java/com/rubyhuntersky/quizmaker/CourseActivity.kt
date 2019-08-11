package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.data.chapter10CourseMaterial
import com.rubyhuntersky.quizmaker.CourseActivity.LessonFragment.Companion.CANCEL_LESSON_EVENT
import com.rubyhuntersky.quizmaker.CourseActivity.LessonFragment.Companion.REVEAL_ANSWER_EVENT
import com.rubyhuntersky.quizmaker.StepFragment.Button
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
                is ViewCourseMsg.StartLesson -> {
                    val activeLessons = mdl.course.getActiveLessons(LocalDateTime.now())
                    if (activeLessons.isEmpty()) {
                        mdl
                    } else {
                        mdl.copy(activeLesson = activeLessons.random())
                    }
                }
                is ViewCourseMsg.CancelLesson -> mdl.copy(activeLesson = null)
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
        if (existingLessonFragment != null) {
            launch { legend.send(ViewCourseMsg.CancelLesson) }
        } else {
            launch { legend.send(ViewCourseMsg.Quit) }
        }
    }

    private fun launchRenderer(legend: Legend<ViewCourseMdl, ViewCourseMsg>) {
        launch {
            val models = legend.toMdls()
            val events = Channel<String>()
            while (!isDestroyed && !models.isClosedForReceive && !events.isClosedForReceive) {
                select<Unit> {
                    models.onReceive { mdl ->
                        renderCourse(mdl.course, events)
                        renderLesson(mdl.activeLesson, events)
                    }
                    events.onReceive { event ->
                        when (event) {
                            "continueCourse" -> legend.send(ViewCourseMsg.StartLesson)
                            "cancelCourse" -> legend.send(ViewCourseMsg.Quit)
                            CANCEL_LESSON_EVENT -> legend.send(ViewCourseMsg.CancelLesson)
                        }
                    }
                }
            }
            stopRendering()
            events.close()
            models.cancel()
        }
    }

    private fun stopRendering() {
        GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
            ?.finishGuidedStepSupportFragments()
    }

    private suspend fun renderCourse(course: Course, events: Channel<String>) {
        courseFragment.control.send(
            StepFragment.Msg.SetView(
                guidance = GuidanceStylist.Guidance(
                    course.title,
                    course.subtitle ?: "",
                    "Lessons: ${course.getActiveLessons(LocalDateTime.now()).size}",
                    getDrawable(R.drawable.ic_launcher_background)
                ),
                buttons = listOf(
                    Button("Lesson", event = "continueCourse", hasNext = true),
                    Button("Cancel", event = "cancelCourse", hasNext = false)
                ),
                events = events
            )
        )
    }

    private suspend fun renderLesson(
        lesson: Lesson?,
        events: Channel<String>
    ) {
        if (lesson == null) {
            existingCourseFragment?.popBackStackToGuidedStepSupportFragment(CourseFragment::class.java, 0)
        } else {
            lessonFragment.control.send(
                StepFragment.Msg.SetView(
                    guidance = GuidanceStylist.Guidance(
                        lesson.prompt,
                        lesson.promptColor ?: "",
                        lesson.lastSeen?.let { "Last seen: $it" } ?: "",
                        null
                    ),
                    buttons = listOf(
                        Button(
                            "Check Answer",
                            event = REVEAL_ANSWER_EVENT,
                            hasNext = true,
                            subtext = "Try writing or saying it in a sentence."
                        ),
                        Button("Back", event = CANCEL_LESSON_EVENT, hasNext = false)
                    ),
                    events = events
                )
            )
        }
    }

    private val courseFragment: CourseFragment
        get() = existingCourseFragment
            ?: CourseFragment().also { GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content) }

    private val existingCourseFragment: CourseFragment?
        get() = supportFragmentManager.fragments.firstOrNull { it is CourseFragment } as? CourseFragment

    private val lessonFragment: LessonFragment
        get() = existingLessonFragment
            ?: LessonFragment().also { GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content) }

    private val existingLessonFragment: LessonFragment?
        get() = supportFragmentManager.fragments.firstOrNull { it is LessonFragment } as? LessonFragment

    class LessonFragment : StepFragment() {
        companion object {
            const val REVEAL_ANSWER_EVENT = "revealAnswer"
            const val CANCEL_LESSON_EVENT = "cancelLesson"
        }
    }

    class CourseFragment : StepFragment()
}
