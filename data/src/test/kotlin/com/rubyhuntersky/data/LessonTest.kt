package com.rubyhuntersky.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

internal class LessonTest {

    private val now = LocalDateTime.now()
    private val past = now - Duration.ofMinutes(10)
    private val morePast = now - Duration.ofMinutes(20)

    private val newLesson = Lesson(
        Semester1CourseMaterial.lessons.first(),
        hardTime = past,
        easyTime = null
    )

    private val attemptedLesson = Lesson(
        Semester1CourseMaterial.lessons.first(),
        hardTime = past,
        easyTime = morePast
    )

    private val learnedLesson = Lesson(
        Semester1CourseMaterial.lessons.first(),
        hardTime = morePast,
        easyTime = past
    )

    @Test
    internal fun lastLearned() {
        assertNull(newLesson.learnedTime)
        assertNull(attemptedLesson.learnedTime)
        assertEquals(past, learnedLesson.learnedTime)
    }

    @Test
    internal fun lessonLearnedOnceRepeatsAfterAbout1Day() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(1)
        val lesson = Lesson(Semester1CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(22), lesson.wakeTime)
    }

    @Test
    internal fun lessonReconfirmedAfter24HoursRepeatsInAbout2Days() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(24)
        val lesson = Lesson(Semester1CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(46), lesson.wakeTime)
    }

    @Test
    internal fun lessonReconfirmedAfter2DaysRepeatsInAbout4Days() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(48)
        val lesson = Lesson(Semester1CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(94), lesson.wakeTime)
    }
}