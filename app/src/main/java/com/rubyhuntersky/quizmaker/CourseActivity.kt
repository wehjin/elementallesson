package com.rubyhuntersky.quizmaker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.quizmaker.android.toRelativeString
import com.rubyhuntersky.quizmaker.app.AppScope
import com.rubyhuntersky.quizmaker.app.TAG
import com.rubyhuntersky.quizmaker.viewcourse.ViewCourseMdl
import com.rubyhuntersky.quizmaker.viewcourse.ViewCourseMsg
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class CourseActivity : FragmentActivity(), CoroutineScope, AppScope, LegendScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    private val legend = findLegend<ViewCourseMdl, ViewCourseMsg>()!!

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        models = legend.startMdls()
        launchRenderer(models, legend)
    }

    private lateinit var models: ReceiveChannel<ViewCourseMdl>

    override fun onStop() {
        models.cancel()
        super.onStop()
    }

    override fun onBackPressed() {
        when {
            getCurrentFragment<AnswerFragment>() != null -> launch { legend.send(ViewCourseMsg.BackToLesson) }
            getCurrentFragment<LessonFragment>() != null -> launch { legend.send(ViewCourseMsg.CancelLesson) }
            else -> launch { legend.send(ViewCourseMsg.Cancel) }
        }
    }

    private fun launchRenderer(
        models: ReceiveChannel<ViewCourseMdl>,
        legend: Legend<ViewCourseMdl, ViewCourseMsg>
    ) {
        Log.d(TAG, "Launching renderer.")
        launch {
            val events = Channel<String>()
            while (!isDestroyed && !models.isClosedForReceive && !events.isClosedForReceive) {
                select<Unit> {
                    models.onReceiveOrClosed { maybeMdl ->
                        maybeMdl.valueOrNull?.let { mdl ->
                            if (mdl.activeCourse != null && mdl.activeLesson != null && mdl.isCheckingAnswer) {
                                requireAnswerFragment().setSight(mdl.activeLesson, events)
                            } else if (mdl.activeCourse != null && mdl.activeLesson != null) {
                                getCurrentFragment<AnswerFragment>()?.let {
                                    Log.d("CourseLegend", "Popping to lesson.")
                                    it.popBackStackToGuidedStepSupportFragment(
                                        LessonFragment::class.java,
                                        0
                                    )
                                }
                                requireLessonFragment().setSight(mdl.activeLesson, events)
                            } else if (mdl.activeCourse != null) {
                                Log.d(TAG, "Active course: ${mdl.activeCourse.title}")
                                getCurrentFragment<LessonFragment>()?.let {
                                    Log.d("CourseLegend", "Popping to course.")
                                    it.popBackStackToGuidedStepSupportFragment(
                                        CourseFragment::class.java,
                                        0
                                    )
                                }
                                requireCourseFragment().setSight(
                                    mdl.activeCourse,
                                    events,
                                    this@CourseActivity
                                )
                            } else {
                                GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(
                                    supportFragmentManager
                                )
                                    ?.finishGuidedStepSupportFragments()
                                this@CourseActivity.finish()
                            }
                        } ?: Unit
                    }
                    events.onReceive { event ->
                        when (event) {
                            CourseFragment.CANCEL_COURSE -> legend.offer(ViewCourseMsg.Cancel)
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
            Log.d(TAG, "Exiting renderer.")
        }
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
            val activeCount = course.activeLessons(LocalDateTime.now()).size
            val firstButton = if (activeCount > 0) {
                listOf(
                    Button(
                        "Next ${min(activeCount, Course.maxLessonsPerSession)}",
                        event = START_LESSON,
                        hasNext = true
                    )
                )
            } else {
                emptyList()
            }
            control.send(
                Msg.SetView(
                    guidance = GuidanceStylist.Guidance(
                        course.title,
                        course.subtitle ?: "",
                        if (activeCount > 0) "$activeCount Active Lessons" else "All Lessons Resting",
                        context.getDrawable(R.mipmap.ic_launcher)
                    ),
                    buttons = firstButton + listOf(
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
                        Button("Repeat", event = ANSWER_HARD, subtext = "Hard. Repeat soon."),
                        Button(
                            "Space",
                            event = ANSWER_EASY,
                            subtext = "Easy. Repeat in ${lesson.restDurationWithEasy(LocalDateTime.now()).toRelativeString()}."
                        )
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
