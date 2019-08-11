package com.rubyhuntersky.quizmaker

import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.Lesson

data class ViewCourseMdl(
    val course: Course,
    val activeLesson: Lesson? = null,
    val isCheckingAnswer: Boolean = false
) {

    override fun toString(): String {
        return "ViewCourseMdl(course=${course.title}, activeLesson=$activeLesson, isCheckingAnswer=$isCheckingAnswer)"
    }
}