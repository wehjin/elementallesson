package com.rubyhuntersky.quizmaker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
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
class CourseActivity : FragmentActivity(), CoroutineScope, AppScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    private val viewCourse
        get() = app.viewCourse

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRenderer(viewCourse)
    }

    override fun onBackPressed() {
        when {
            getCurrentFragment<AnswerFragment>() != null -> launch { viewCourse.send(ViewCourseMsg.BackToLesson) }
            getCurrentFragment<LessonFragment>() != null -> launch { viewCourse.send(ViewCourseMsg.CancelLesson) }
            else -> finish()
        }
    }

    private fun launchRenderer(legend: Legend<ViewCourseMdl, ViewCourseMsg>) {
        launch {
            val models = legend.toMdls()
            val events = Channel<String>()
            while (!isDestroyed && !models.isClosedForReceive && !events.isClosedForReceive) {
                select<Unit> {
                    models.onReceive { mdl ->
                        if (mdl.activeLesson != null && mdl.isCheckingAnswer) {
                            requireAnswerFragment().setSight(mdl.activeLesson, events)
                        } else if (mdl.activeLesson != null) {
                            getCurrentFragment<AnswerFragment>()?.let {
                                Log.d("CourseLegend", "Popping to lesson.")
                                it.popBackStackToGuidedStepSupportFragment(LessonFragment::class.java, 0)
                            }
                            requireLessonFragment().setSight(mdl.activeLesson, events)
                        } else {
                            getCurrentFragment<LessonFragment>()?.let {
                                Log.d("CourseLegend", "Popping to course.")
                                it.popBackStackToGuidedStepSupportFragment(CourseFragment::class.java, 0)
                            }
                            requireCourseFragment().setSight(mdl.course, events, this@CourseActivity)
                        }
                    }
                    events.onReceive { event ->
                        when (event) {
                            CourseFragment.CANCEL_COURSE -> finish()
                            CourseFragment.RESET_COURSE -> legend.offer(ViewCourseMsg.Reset)
                            CourseFragment.START_LESSON -> legend.offer(ViewCourseMsg.StartLesson)
                            LessonFragment.CANCEL_LESSON -> legend.offer(ViewCourseMsg.CancelLesson)
                            LessonFragment.CHECK_ANSWER -> legend.offer(ViewCourseMsg.CheckAnswer)
                            AnswerFragment.CANCEL_ANSWER -> legend.offer(ViewCourseMsg.BackToLesson)
                            AnswerFragment.ANSWER_HARD -> legend.offer(ViewCourseMsg.RecordHard)
                            AnswerFragment.ANSWER_EASY -> legend.offer(ViewCourseMsg.RecordEasy)
                        }
                    }
                }
            }
            dropFragments()
            events.close()
            models.cancel()
        }
    }

    private fun dropFragments() {
        GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
            ?.finishGuidedStepSupportFragments()
    }

    private fun requireCourseFragment(): CourseFragment = getCurrentFragment()
        ?: CourseFragment().also {
            Log.d("CourseLegend", "ADDING new CourseFragment")
            GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content)
        }

    private fun requireLessonFragment(): LessonFragment = getCurrentFragment()
        ?: LessonFragment().also {
            requireCourseFragment()
            Log.d("CourseLegend", "ADDING new LessonFragment")
            GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content)
        }

    private fun requireAnswerFragment(): AnswerFragment = getCurrentFragment()
        ?: AnswerFragment().also {
            Log.d("CourseLegend", "ADDING new AnswerFragment")
            GuidedStepSupportFragment.add(supportFragmentManager, it, android.R.id.content)
        }

    private inline fun <reified T : StepFragment> getCurrentFragment(): T? {
        return GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager) as? T
    }


    class CourseFragment : StepFragment() {
        suspend fun setSight(course: Course, events: Channel<String>, context: Context) {
            control.send(
                Msg.SetView(
                    guidance = GuidanceStylist.Guidance(
                        course.title,
                        course.subtitle ?: "",
                        "Lessons Remaining: ${course.getActiveLessons(LocalDateTime.now()).size}",
                        context.getDrawable(R.drawable.ic_launcher_background)
                    ),
                    buttons = listOf(
                        Button("Go", event = START_LESSON, hasNext = true),
                        Button("Close", event = CANCEL_COURSE, hasNext = false),
                        Button("Reset", event = RESET_COURSE, hasNext = false)
                    ),
                    events = events
                )
            )
        }

        companion object {
            const val START_LESSON = "startLesson"
            const val CANCEL_COURSE = "cancelCourse"
            const val RESET_COURSE = "resetCourse"
        }
    }

    class LessonFragment : StepFragment() {

        suspend fun setSight(lesson: Lesson, events: Channel<String>) {
            control.send(
                Msg.SetView(
                    guidance = GuidanceStylist.Guidance(
                        lesson.prompt,
                        lesson.promptColor ?: "",
                        lesson.lastSeen?.let { "Last seen: $it" } ?: "",
                        null
                    ),
                    buttons = listOf(
                        Button(
                            "Check My Answer",
                            event = CHECK_ANSWER,
                            hasNext = true,
                            subtext = "Try writing or saying it in a sentence."
                        ),
                        Button("Back", event = CANCEL_LESSON, hasNext = false)
                    ),
                    events = events
                )
            )
        }

        companion object {
            const val CHECK_ANSWER = "checkAnswer"
            const val CANCEL_LESSON = "cancelLesson"
        }
    }

    class AnswerFragment : StepFragment() {

        suspend fun setSight(lesson: Lesson, events: Channel<String>) {
            control.send(
                Msg.SetView(
                    guidance = GuidanceStylist.Guidance(
                        lesson.response,
                        lesson.responseColor ?: "",
                        lesson.prompt,
                        null
                    ),
                    buttons = listOf(
                        Button("Back", event = CANCEL_ANSWER),
                        Button("Hard", event = ANSWER_HARD, subtext = "Repeat soon."),
                        Button("Easy", event = ANSWER_EASY, subtext = "Repeat after I've forgotten.")
                    ),
                    events = events
                )
            )
        }

        companion object {
            const val ANSWER_EASY = "recordEasy"
            const val ANSWER_HARD = "recordHard"
            const val CANCEL_ANSWER = "backToLesson"
        }
    }
}
