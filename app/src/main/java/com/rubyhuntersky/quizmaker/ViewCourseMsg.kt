package com.rubyhuntersky.quizmaker

sealed class ViewCourseMsg {
    object Quit : ViewCourseMsg()
    object StartLesson : ViewCourseMsg()
    object CancelLesson : ViewCourseMsg()
}