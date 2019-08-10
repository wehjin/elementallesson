package com.rubyhuntersky.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Course(
    val name: String,
    val lessons: Set<Lesson>
) {

    fun toActiveLessons(time: LocalDateTime): Set<Lesson> = lessons.filter { it.wakeTime.isBefore(time) }.toSet()

    fun update(lesson: Lesson): Course = copy(
        lessons = lessons.toMutableSet()
            .also { lessons ->
                lessons.removeIf { it.material == lesson.material }
                lessons.add(lesson)
            })

    companion object {
        fun start(courseMaterial: CourseMaterial, time: LocalDateTime) = Course(
            name = courseMaterial.name,
            lessons = courseMaterial.lessons.map { Lesson(it, time) }.toSet()
        )
    }
}
