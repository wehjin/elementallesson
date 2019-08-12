package com.rubyhuntersky.quizmaker

import android.app.Application
import android.util.Log
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.chapter10CourseMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

interface AppScope {
    fun getApplication(): Application
}

@ExperimentalCoroutinesApi
val AppScope.app: App
    get() = getApplication() as App

@Suppress("unused")
val AppScope.TAG: String
    get() = this::class.java.simpleName

@ExperimentalCoroutinesApi
class App : Application(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = job
    private val storeChannel = Channel<StoreMsg>(10)

    val viewCourse =
        legendOf<ViewCourseMdl, ViewCourseMsg> { mdls, msgs ->
            storeCtl.send(StoreMsg.ReadCourse { course ->
                launch {
                    var mdl = ViewCourseMdl(course).also { mdls.send(it) }
                    for (msg in msgs) {
                        Log.d("CourseLegend", "MSG: $msg, MDL: $mdl")
                        mdl = when (msg) {
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
                                mdl.activeLesson?.let { lesson ->
                                    val newLesson = lesson.setHard(LocalDateTime.now())
                                    val newCourse = mdl.course.update(newLesson).also {
                                        storeCtl.send(StoreMsg.WriteCourse(it))
                                    }
                                    val newActiveLessons = newCourse.getActiveLessons(LocalDateTime.now())
                                    val newActiveLesson =
                                        if (newActiveLessons.isEmpty()) null else newActiveLessons.random()
                                    mdl.copy(
                                        course = newCourse,
                                        activeLesson = newActiveLesson,
                                        isCheckingAnswer = false
                                    )
                                } ?: mdl
                            }
                            is ViewCourseMsg.RecordEasy -> {
                                mdl.activeLesson?.let { lesson ->
                                    val newLesson = lesson.setEasy(LocalDateTime.now())
                                    val newCourse = mdl.course.update(newLesson).also {
                                        storeCtl.send(StoreMsg.WriteCourse(it))
                                    }
                                    val newActiveLessons = newCourse.getActiveLessons(LocalDateTime.now())
                                    val newActiveLesson =
                                        if (newActiveLessons.isEmpty()) null else newActiveLessons.random()
                                    mdl.copy(
                                        course = newCourse,
                                        activeLesson = newActiveLesson,
                                        isCheckingAnswer = false
                                    )
                                } ?: mdl
                            }
                        }
                        mdls.send(mdl)
                    }
                    mdls.close()
                }
            })
        }

    sealed class StoreMsg {
        data class ReadCourse(val receive: (Course) -> Unit) : StoreMsg()
        data class WriteCourse(val course: Course) : StoreMsg()
    }

    private val storeCtl
        get() = storeChannel as SendChannel<StoreMsg>

    override fun onCreate() {
        super.onCreate()
        launch {
            val courseFile = File(filesDir, "activeCourse")
            var course = CourseStore.read(courseFile) ?: Course.start(chapter10CourseMaterial, LocalDateTime.now())
            while (!storeChannel.isClosedForReceive) {
                when (val msg = storeChannel.receive()) {
                    is StoreMsg.ReadCourse -> msg.receive(course)
                    is StoreMsg.WriteCourse -> {
                        course = msg.course
                        CourseStore.write(msg.course, courseFile)
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        job.cancel()
        super.onTerminate()
    }
}

