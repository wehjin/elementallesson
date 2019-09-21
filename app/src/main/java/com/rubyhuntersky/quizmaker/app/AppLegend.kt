package com.rubyhuntersky.quizmaker.app

import android.util.Log
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.data.Study
import com.rubyhuntersky.mepl.Mepl
import com.rubyhuntersky.quizmaker.Legend
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.startLegend
import com.rubyhuntersky.quizmaker.tools.MaterialLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.time.LocalDateTime

sealed class AppMdl {

    abstract val study: Study

    data class ActiveStudy(
        override val study: Study
    ) : AppMdl() {

        fun replaceCourse(course: Course): ActiveStudy {
            val newStudy = study.replaceCourse(course)
            return copy(study = newStudy)
        }
    }

    data class CourseViewing(
        val course: Course,
        val studyMdl: ActiveStudy
    ) : AppMdl() {
        override val study: Study get() = studyMdl.study

        fun updateLesson(lesson: Lesson): CourseViewing {
            val newCourse = course.replaceLesson(lesson)
            val newStudyMdl = studyMdl.replaceCourse(newCourse)
            return copy(course = newCourse, studyMdl = newStudyMdl)
        }
    }

    data class LessonLearning(
        val lesson: Lesson,
        val activeLessonList: List<Lesson>,
        val courseViewing: CourseViewing
    ) : AppMdl() {
        val activeCourse: Course get() = courseViewing.course
        override val study: Study get() = courseViewing.study
    }

    data class AnswerChecking(
        val lessonLearning: LessonLearning
    ) : AppMdl() {
        val activeLesson: Lesson get() = lessonLearning.lesson
        private val activeLessons: List<Lesson> get() = lessonLearning.activeLessonList
        val activeCourse: Course get() = lessonLearning.activeCourse
        override val study: Study get() = lessonLearning.study

        fun updateLesson(modified: Lesson): AppMdl {
            val newCourseViewing = lessonLearning.courseViewing.updateLesson(modified)
            val lessonsWithModified =
                activeLessons.map { if (it.id == modified.id) modified else it }
            val newActiveLessons =
                Course.toActiveOrderedLessons(lessonsWithModified, LocalDateTime.now())
            return when (newActiveLessons.size) {
                0 -> newCourseViewing
                1 -> LessonLearning(newActiveLessons.first(), newActiveLessons, newCourseViewing)
                else -> {
                    val nextLesson = newActiveLessons.filter { it.id != modified.id }.random()
                    LessonLearning(nextLesson, newActiveLessons, newCourseViewing)
                }
            }
        }
    }
}

sealed class AppMsg {

    data class SelectCourse(val course: Course) : AppMsg()
    object CancelCourse : AppMsg()
    object ResetCourse : AppMsg()
    object StartLessons : AppMsg()
    object CancelLessons : AppMsg()
    object PlayClip : AppMsg()
    object CheckAnswer : AppMsg()
    object CancelAnswer : AppMsg()
    object RepeatLesson : AppMsg()
    object SpaceLesson : AppMsg()
}

@ExperimentalCoroutinesApi
fun LegendScope.startAppLegend(storeCtl: SendChannel<StoreMsg>): Legend<AppMdl, AppMsg> =
    startLegend { mdls, msgs ->
        val tag = "AppLegend"
        val study = Channel<Study>().let {
            storeCtl.send(StoreMsg.ReadStudy(it))
            it.receive()
        }
        var mdl: AppMdl = AppMdl.ActiveStudy(study).also { mdls.send(it) }
        for (msg in msgs) {
            val oldMdl = mdl
            mdl = when {
                msg is AppMsg.SelectCourse -> {
                    Log.d(tag, "Selected course: ${msg.course.title}")
                    AppMdl.CourseViewing(msg.course, AppMdl.ActiveStudy(oldMdl.study))
                }
                oldMdl is AppMdl.CourseViewing && msg is AppMsg.CancelCourse -> oldMdl.studyMdl
                oldMdl is AppMdl.CourseViewing && msg is AppMsg.ResetCourse -> {
                    val courseMaterial =
                        MaterialLoader.basicDegreeMaterial.courses.find { it.title == oldMdl.course.title }
                    courseMaterial?.let { material ->
                        val newCourse = Course.start(material, LocalDateTime.now())
                        val newStudy = oldMdl.studyMdl.study.replaceCourse(newCourse)
                            .also { storeCtl.send(StoreMsg.WriteStudy(it)) }
                        val newStudyMdl = oldMdl.studyMdl.copy(study = newStudy)
                        AppMdl.CourseViewing(newCourse, newStudyMdl)
                    } ?: oldMdl.also {
                        Log.d(
                            tag,
                            "No course material found for course reset: ${oldMdl.course.title}"
                        )
                    }
                }
                oldMdl is AppMdl.CourseViewing && msg is AppMsg.StartLessons -> {
                    val activeLessons = oldMdl.course.lessonList(LocalDateTime.now())
                    if (activeLessons.isEmpty()) {
                        oldMdl
                    } else {
                        AppMdl.LessonLearning(activeLessons.random(), activeLessons, oldMdl)
                    }
                }
                oldMdl is AppMdl.LessonLearning && msg is AppMsg.CancelLessons -> oldMdl.courseViewing
                oldMdl is AppMdl.LessonLearning && msg is AppMsg.PlayClip -> {
                    oldMdl.also { it.lesson.clipBase?.let(Mepl::playClip) }
                }
                oldMdl is AppMdl.LessonLearning && msg is AppMsg.CheckAnswer -> AppMdl.AnswerChecking(
                    oldMdl
                )
                oldMdl is AppMdl.AnswerChecking && msg is AppMsg.CancelAnswer -> oldMdl.lessonLearning
                oldMdl is AppMdl.AnswerChecking && msg is AppMsg.RepeatLesson -> {
                    val repeatingLesson = oldMdl.activeLesson.setHard(LocalDateTime.now())
                    oldMdl.updateLesson(repeatingLesson)
                        .also { storeCtl.send(StoreMsg.WriteStudy(it.study)) }
                }
                oldMdl is AppMdl.AnswerChecking && msg is AppMsg.SpaceLesson -> {
                    val spacedLesson = oldMdl.activeLesson.setEasy(LocalDateTime.now())
                    oldMdl.updateLesson(spacedLesson)
                        .also { storeCtl.send(StoreMsg.WriteStudy(it.study)) }
                }
                else -> {
                    Log.e(
                        tag,
                        "Invalid update: MSG: ${msg::class.java.simpleName} MDL: ${oldMdl::class.java.simpleName}"
                    )
                    oldMdl
                }
            }
            mdls.send(mdl)
        }
        mdls.close()
        mdl
    }