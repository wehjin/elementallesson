package com.rubyhuntersky.quizmaker

sealed class ViewCourseMsg {
    object Quit : ViewCourseMsg()
    object StartLesson : ViewCourseMsg()
    object CancelLesson : ViewCourseMsg()
    object CheckAnswer : ViewCourseMsg()
    object BackToLesson : ViewCourseMsg()
    object RecordEasy : ViewCourseMsg()
    object RecordHard : ViewCourseMsg()

    override fun toString(): String = this::class.java.simpleName
}