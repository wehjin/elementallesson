package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.core.CourseMaterial
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class Course(
    val title: String,
    val subtitle: String?,
    val lessons: Set<Lesson>
) {

    fun lessonList(time: LocalDateTime): List<Lesson> = toActiveOrderedLessons(lessons, time)
    fun getActiveLessons(time: LocalDateTime): Set<Lesson> = lessons.filter { it.isAwake(time) }.toSet()

    fun replaceLesson(lesson: Lesson): Course = copy(
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

        const val maxLessonsPerSession = 43

        fun toActiveOrderedLessons(lessons: Iterable<Lesson>, time: LocalDateTime): List<Lesson> {
            return lessons.filter { it.isAwake(time) }
                .sortedWith(Comparator { a, b ->
                    val aIsLearned = a.isLearned
                    val bIsLearned = b.isLearned
                    when {
                        aIsLearned && !bIsLearned -> -1
                        bIsLearned && !aIsLearned -> 1
                        else -> a.material.level.compareTo(b.material.level)
                    }
                })
                .let { if (it.size > maxLessonsPerSession) it.subList(0, maxLessonsPerSession) else it }
        }
    }
}
