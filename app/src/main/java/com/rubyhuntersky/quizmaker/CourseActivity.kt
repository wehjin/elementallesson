package com.rubyhuntersky.quizmaker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
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
            Log.d("CourseLegend", "MSG: $msg, MDL: $mdl")
            mdl = when (msg) {
                is ViewCourseMsg.Quit -> mdl.also { msgs.close() }
                is ViewCourseMsg.StartLesson -> {
                    val activeLessons = mdl.course.getActiveLessons(LocalDateTime.now())
                    if (activeLessons.isEmpty()) {
                        mdl
                    } else {
                        mdl.copy(activeLesson = activeLessons.random(), isCheckingAnswer = false)
                    }
                }
                is ViewCourseMsg.CancelLesson -> mdl.copy(activeLesson = null, isCheckingAnswer = false)
                is ViewCourseMsg.CheckAnswer -> mdl.copy(isCheckingAnswer = true)
                is ViewCourseMsg.BackToLesson -> mdl.copy(isCheckingAnswer = false)
                is ViewCourseMsg.RecordHard -> {
                    mdl.activeLesson?.let {
                        val newLesson = it.setHard(LocalDateTime.now())
                        val newCourse = mdl.course.update(newLesson)
                        val newActiveLessons = newCourse.getActiveLessons(LocalDateTime.now())
                        val newActiveLesson = if (newActiveLessons.isEmpty()) null else newActiveLessons.random()
                        mdl.copy(course = newCourse, activeLesson = newActiveLesson, isCheckingAnswer = false)
                    } ?: mdl
                }
                is ViewCourseMsg.RecordEasy -> {
                    mdl.activeLesson?.let {
                        val newLesson = it.setEasy(LocalDateTime.now())
                        val newCourse = mdl.course.update(newLesson)
                        val newActiveLessons = newCourse.getActiveLessons(LocalDateTime.now())
                        val newActiveLesson = if (newActiveLessons.isEmpty()) null else newActiveLessons.random()
                        mdl.copy(course = newCourse, activeLesson = newActiveLesson, isCheckingAnswer = false)
                    } ?: mdl
                }
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
        when {
            getCurrentFragment<AnswerFragment>() != null -> launch { legend.send(ViewCourseMsg.BackToLesson) }
            getCurrentFragment<LessonFragment>() != null -> launch { legend.send(ViewCourseMsg.CancelLesson) }
            else -> launch { legend.send(ViewCourseMsg.Quit) }
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
                            "continueCourse" -> legend.offer(ViewCourseMsg.StartLesson)
                            "cancelCourse" -> legend.offer(ViewCourseMsg.Quit)
                            LessonFragment.CANCEL_LESSON -> legend.offer(ViewCourseMsg.CancelLesson)
                            LessonFragment.CHECK_ANSWER -> legend.offer(ViewCourseMsg.CheckAnswer)
                            AnswerFragment.BACK_TO_LESSON -> legend.offer(ViewCourseMsg.BackToLesson)
                            AnswerFragment.WAS_HARD -> legend.offer(ViewCourseMsg.RecordHard)
                            AnswerFragment.WAS_EASY -> legend.offer(ViewCourseMsg.RecordEasy)
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
                        Button("Go", event = "continueCourse", hasNext = true),
                        Button("Cancel", event = "cancelCourse", hasNext = false)
                    ),
                    events = events
                )
            )
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
                        Button("Back", event = BACK_TO_LESSON),
                        Button("Hard", event = WAS_HARD, subtext = "Repeat soon."),
                        Button("Easy", event = WAS_EASY, subtext = "Repeat after I've forgotten.")
                    ),
                    events = events
                )
            )
        }

        companion object {
            const val WAS_EASY = "recordEasy"
            const val WAS_HARD = "recordHard"
            const val BACK_TO_LESSON = "backToLesson"
        }
    }
}
