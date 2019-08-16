package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.Sem1Chap10CourseMaterial
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

internal class CourseTest {

    @Test
    internal fun startsWithActiveLessons() {
        val now = LocalDateTime.now()
        val course = Course.start(Sem1Chap10CourseMaterial, now)
        val activeLessons = course.getActiveLessons(now)
        assertEquals(course.lessons.size, activeLessons.size)
    }

    @Test
    internal fun lessonBecomesInactiveAfterEasy() {
        val now = LocalDateTime.now()
        val course = Course.start(Sem1Chap10CourseMaterial, now - Duration.ofHours(1))
        val activeLessons = course.getActiveLessons(now)
        val newCourse = course.replaceLesson(activeLessons.first().setEasy(now))
        val newActiveLessons = newCourse.getActiveLessons(now + Duration.ofHours(1))
        assertEquals(activeLessons.size - 1, newActiveLessons.size)
    }
}