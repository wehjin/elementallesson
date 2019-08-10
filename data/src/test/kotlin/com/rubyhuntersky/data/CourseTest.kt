package com.rubyhuntersky.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

internal class CourseTest {

    @Test
    internal fun lessonBecomesInactiveAfterEasy() {
        val now = LocalDateTime.now()
        val course = Course.start(chapter10CourseMaterial, now - Duration.ofHours(1))
        val activeLessons = course.toActiveLessons(now)
        val newCourse = course.update(activeLessons.first().setEasy(now))
        val newActiveLessons = newCourse.toActiveLessons(now + Duration.ofHours(1))
        assertEquals(activeLessons.size - 1, newActiveLessons.size)
    }
}