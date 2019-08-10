package com.rubyhuntersky.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

internal class LessonTest {

    @Test
    internal fun lessonLearnedOnceRepeatsAfterAbout1Day() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(1)
        val lesson = Lesson(chapter10CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(22), lesson.wakeTime)
    }

    @Test
    internal fun lessonReconfirmedAfter24HoursRepeatsInAbout2Days() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(24)
        val lesson = Lesson(chapter10CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(46), lesson.wakeTime)
    }

    @Test
    internal fun lessonReconfirmedAfter2DaysRepeatsInAbout4Days() {
        val easy = LocalDateTime.now()
        val hard = easy - Duration.ofHours(48)
        val lesson = Lesson(chapter10CourseMaterial.lessons.first(), hard, easy)
        assertEquals(easy + Duration.ofHours(94), lesson.wakeTime)
    }
}