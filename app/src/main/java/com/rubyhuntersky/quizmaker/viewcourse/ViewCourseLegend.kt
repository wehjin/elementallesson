package com.rubyhuntersky.quizmaker.viewcourse

import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.quizmaker.Legend
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.app.AppMdl
import com.rubyhuntersky.quizmaker.app.AppMsg
import com.rubyhuntersky.quizmaker.startLegend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.selects.select

data class ViewCourseMdl(
    val activeCourse: Course? = null,
    val activeLesson: Lesson? = null,
    val isCheckingAnswer: Boolean = false
) {

    override fun toString(): String = "ViewCourseMdl(" +
            "course=${activeCourse?.title ?: "No course"}" +
            ", activeLesson=$activeLesson" +
            ", isCheckingAnswer=$isCheckingAnswer" +
            ")"
}

sealed class ViewCourseMsg {
    object Cancel : ViewCourseMsg()
    object Reset : ViewCourseMsg()
    object StartLesson : ViewCourseMsg()
    object CancelLesson : ViewCourseMsg()
    object PlayClip : ViewCourseMsg()
    object CheckAnswer : ViewCourseMsg()
    object BackToLesson : ViewCourseMsg()
    object RecordEasy : ViewCourseMsg()
    object RecordHard : ViewCourseMsg()

    override fun toString(): String = this::class.java.simpleName
}

@ExperimentalCoroutinesApi
fun LegendScope.startViewCourseLegend(appLegend: Legend<AppMdl, AppMsg>): Legend<ViewCourseMdl, ViewCourseMsg> =
    startLegend { mdls, msgs ->
        val appMdls = appLegend.startMdls()
        var viewCourseMdl = ViewCourseMdl()
        while (!appMdls.isClosedForReceive && !msgs.isClosedForReceive) {
            select<Unit> {
                appMdls.onReceive { appMdl ->
                    viewCourseMdl = when (appMdl) {
                        is AppMdl.ActiveStudy -> ViewCourseMdl(
                            null
                        )
                        is AppMdl.ActiveCourse -> ViewCourseMdl(
                            appMdl.course,
                            null,
                            false
                        )
                        is AppMdl.ActiveLesson -> ViewCourseMdl(
                            appMdl.activeCourse,
                            appMdl.lesson,
                            false
                        )
                        is AppMdl.ActiveAnswer -> ViewCourseMdl(
                            appMdl.activeCourse,
                            appMdl.activeLesson,
                            true
                        )
                    }
                    mdls.send(viewCourseMdl)
                }
                msgs.onReceive { msg ->
                    when (msg) {
                        is ViewCourseMsg.Cancel -> appLegend.send(AppMsg.CancelCourse)
                        is ViewCourseMsg.Reset -> appLegend.send(AppMsg.ResetCourse)
                        is ViewCourseMsg.StartLesson -> appLegend.send(AppMsg.StartLessons)
                        is ViewCourseMsg.CancelLesson -> appLegend.send(AppMsg.CancelLessons)
                        is ViewCourseMsg.PlayClip -> appLegend.send(AppMsg.PlayClip)
                        is ViewCourseMsg.CheckAnswer -> appLegend.send(AppMsg.CheckAnswer)
                        is ViewCourseMsg.BackToLesson -> appLegend.send(AppMsg.CancelAnswer)
                        is ViewCourseMsg.RecordHard -> appLegend.send(AppMsg.RepeatLesson)
                        is ViewCourseMsg.RecordEasy -> appLegend.send(AppMsg.SpaceLesson)
                    }
                }
            }
        }
        appMdls.cancel()
        mdls.close()
        viewCourseMdl
    }