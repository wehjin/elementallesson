package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.CourseMaterial
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class Course(
    val title: String,
    val subtitle: String?,
    val lessons: Set<Lesson>
) {

    fun getActiveLessons(time: LocalDateTime): Set<Lesson> = lessons.filter {
        val wakeTime = it.wakeTime
        val isActive = wakeTime.isBefore(time)
        isActive
    }.toSet()

    fun update(lesson: Lesson): Course = copy(
        lessons = lessons.toMutableSet()
            .also { lessons ->
                lessons.removeIf { it.material == lesson.material }
                lessons.add(lesson)
            })

    companion object {
        fun start(courseMaterial: CourseMaterial, time: LocalDateTime) = Course(
            title = courseMaterial.title,
            subtitle = courseMaterial.subtitle,
            lessons = courseMaterial.lessons.map { Lesson(it, time - Duration.ofMinutes(2)) }.toSet()
        )
    }
}
