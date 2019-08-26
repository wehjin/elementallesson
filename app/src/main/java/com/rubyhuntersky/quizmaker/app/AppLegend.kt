package com.rubyhuntersky.quizmaker.app

import android.util.Log
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.data.Study
import com.rubyhuntersky.data.material.BasicDegreeMaterial
import com.rubyhuntersky.quizmaker.Legend
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.startLegend
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

    data class ActiveCourse(
        val course: Course,
        val studyMdl: ActiveStudy
    ) : AppMdl() {
        override val study: Study get() = studyMdl.study

        fun replaceLesson(lesson: Lesson): ActiveCourse {
            val newCourse = course.replaceLesson(lesson)
            val newStudyMdl = studyMdl.replaceCourse(newCourse)
            return copy(course = newCourse, studyMdl = newStudyMdl)
        }
    }

    data class ActiveLesson(
        val lesson: Lesson,
        val activeLessonList: List<Lesson>,
        val courseMdl: ActiveCourse
    ) : AppMdl() {
        val activeCourse: Course get() = courseMdl.course
        override val study: Study get() = courseMdl.study
    }

    data class ActiveAnswer(
        val lessonMdl: ActiveLesson
    ) : AppMdl() {
        val activeLesson: Lesson get() = lessonMdl.lesson
        val activeLessons: List<Lesson> get() = lessonMdl.activeLessonList
        val activeCourse: Course get() = lessonMdl.activeCourse
        override val study: Study get() = lessonMdl.study
    }
}

sealed class AppMsg {

    data class SelectCourse(val course: Course) : AppMsg()
    object CancelCourse : AppMsg()
    object ResetCourse : AppMsg()
    object StartLessons : AppMsg()
    object CancelLessons : AppMsg()
    object CheckAnswer : AppMsg()
    object CancelAnswer : AppMsg()
    object RepeatLesson : AppMsg()
    object SpaceLesson : AppMsg()
}

@ExperimentalCoroutinesApi
fun LegendScope.startAppLegend(storeCtl: SendChannel<StoreMsg>): Legend<AppMdl, AppMsg> = startLegend { mdls, msgs ->
    val tag = "AppLegend"
    val studyChannel = Channel<Study>()
    storeCtl.send(StoreMsg.ReadStudy(studyChannel))
    var mdl: AppMdl = AppMdl.ActiveStudy(studyChannel.receive()).also { mdls.send(it) }
    for (msg in msgs) {
        val oldMdl = mdl
        mdl = when {
            msg is AppMsg.SelectCourse -> {
                Log.d(tag, "Selected course: ${msg.course.title}")
                AppMdl.ActiveCourse(msg.course, AppMdl.ActiveStudy(oldMdl.study))
            }
            oldMdl is AppMdl.ActiveCourse && msg is AppMsg.CancelCourse -> oldMdl.studyMdl
            oldMdl is AppMdl.ActiveCourse && msg is AppMsg.ResetCourse -> {
                val courseMaterial = BasicDegreeMaterial.courses.find { it.title == oldMdl.course.title }
                courseMaterial?.let { material ->
                    val newCourse = Course.start(material, LocalDateTime.now())
                    val newStudy = oldMdl.studyMdl.study.replaceCourse(newCourse)
                        .also { storeCtl.send(StoreMsg.WriteStudy(it)) }
                    val newStudyMdl = oldMdl.studyMdl.copy(study = newStudy)
                    AppMdl.ActiveCourse(newCourse, newStudyMdl)
                } ?: oldMdl.also {
                    Log.d(tag, "No course material found for course reset: ${oldMdl.course.title}")
                }
            }
            oldMdl is AppMdl.ActiveCourse && msg is AppMsg.StartLessons -> {
                val activeLessons = oldMdl.course.lessonList(LocalDateTime.now())
                if (activeLessons.isEmpty()) {
                    oldMdl
                } else {
                    AppMdl.ActiveLesson(activeLessons.first(), activeLessons, oldMdl)
                }
            }
            oldMdl is AppMdl.ActiveLesson && msg is AppMsg.CancelLessons -> oldMdl.courseMdl
            oldMdl is AppMdl.ActiveLesson && msg is AppMsg.CheckAnswer -> AppMdl.ActiveAnswer(oldMdl)
            oldMdl is AppMdl.ActiveAnswer && msg is AppMsg.CancelAnswer -> oldMdl.lessonMdl
            oldMdl is AppMdl.ActiveAnswer && msg is AppMsg.RepeatLesson -> {
                val newLesson = oldMdl.activeLesson.setHard(LocalDateTime.now())
                val newCourseMdl = oldMdl.lessonMdl.courseMdl.replaceLesson(newLesson)
                    .also { storeCtl.send(StoreMsg.WriteStudy(it.study)) }
                val newActiveLessons = Course.toActiveOrderedLessons(
                    lessons = oldMdl.activeLessons.map { if (it.material == newLesson.material) newLesson else it },
                    time = LocalDateTime.now()
                )
                if (newActiveLessons.isEmpty()) {
                    newCourseMdl
                } else {
                    val otherLessons = newActiveLessons.filter { it.id != newLesson.id }
                    val newActiveLesson =
                        if (otherLessons.isEmpty()) newActiveLessons.random() else otherLessons.random()
                    AppMdl.ActiveLesson(newActiveLesson, newActiveLessons, newCourseMdl)
                }
            }
            oldMdl is AppMdl.ActiveAnswer && msg is AppMsg.SpaceLesson -> {
                val newLesson = oldMdl.activeLesson.setEasy(LocalDateTime.now())
                val newCourseMdl = oldMdl.lessonMdl.courseMdl.replaceLesson(newLesson)
                    .also { storeCtl.send(StoreMsg.WriteStudy(it.study)) }
                val newActiveLessons = Course.toActiveOrderedLessons(
                    lessons = oldMdl.activeLessons.filter { it.material != newLesson.material },
                    time = LocalDateTime.now()
                )
                if (newActiveLessons.isEmpty()) {
                    newCourseMdl
                } else {
                    AppMdl.ActiveLesson(newActiveLessons.random(), newActiveLessons, newCourseMdl)
                }
            }
            else -> {
                Log.e(tag, "Invalid update: MSG: ${msg::class.java.simpleName} MDL: ${oldMdl::class.java.simpleName}")
                oldMdl
            }
        }
        mdls.send(mdl)
    }
    mdls.close()
    mdl
}