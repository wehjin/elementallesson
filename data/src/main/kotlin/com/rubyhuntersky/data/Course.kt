package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.core.CourseMaterial
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.min

@Serializable
data class Course(
    val title: String,
    val subtitle: String?,
    val lessons: Set<Lesson>
) {
    val fullTitle: String get() = subtitle?.let { "$title: $it" } ?: title

    fun pickLessonsForSession(time: LocalDateTime): List<Lesson> {
        val activeLessons = toActiveOrderedLessons(lessons, time)
        return if (activeLessons.size <= maxLessonsPerSession) {
            activeLessons
        } else {
            val (canReview, canLearn) = activeLessons.partition { it.isLearned }
            val takeFromReview = min(maxLessonsPerSession / 2, canReview.size)
            val takeFromLearn = min(maxLessonsPerSession - takeFromReview, canLearn.size)
            val review = canReview.sortedByDescending { it.learnedTime }.take(takeFromReview)
            val learn = canLearn.shuffled().take(takeFromLearn)
            return review + learn
        }
    }

    fun activeLessons(time: LocalDateTime): Set<Lesson> {
        return lessons.filter { it.isAwake(time) }.toSet()
    }

    fun replaceLesson(lesson: Lesson): Course = copy(
        lessons = lessons.toMutableSet()
            .also { lessons ->
                lessons.removeIf { it.material == lesson.material }
                lessons.add(lesson)
            })

    fun mergeInto(previous: Course?): Course {
        return previous?.let { previousCourse ->
            val latestLessons = this.lessons.associateBy(Lesson::id)
            val previousLessonKeys = previousCourse.lessons.map(Lesson::id).toSet()
            val newIds = latestLessons.keys - previousLessonKeys
            val newLessons = newIds.mapNotNull { latestLessons[it] }
            val keepIds = latestLessons.keys - newIds
            val keepLessons = latestLessons.filterKeys { keepIds.contains(it) }
            val updatedLessons =
                previousCourse.lessons.mapNotNull { keepLessons[it.id]?.mergeInto(it) }
            previousCourse.copy(lessons = (updatedLessons + newLessons).toSet())
        } ?: this
    }

    companion object {
        fun start(courseMaterial: CourseMaterial, time: LocalDateTime) = Course(
            title = courseMaterial.title,
            subtitle = courseMaterial.subtitle,
            lessons = courseMaterial.lessons.map {
                Lesson(
                    material = it,
                    hardTime = time - Duration.ofMinutes(2)
                )
            }.toSet()
        )

        const val maxLessonsPerSession = 20

        fun toActiveOrderedLessons(lessons: Iterable<Lesson>, time: LocalDateTime): List<Lesson> {
            return orderByLearnedFirstThenLowestLevel(filterAwakeOnly(lessons, time))
        }

        private fun filterAwakeOnly(lessons: Iterable<Lesson>, time: LocalDateTime): List<Lesson> {
            return lessons.filter { it.isAwake(time) }
        }

        private fun orderByLearnedFirstThenLowestLevel(lessons: List<Lesson>): List<Lesson> {
            return lessons
                .sortedWith(Comparator { a, b ->
                    val aIsLearned = a.isLearned
                    val bIsLearned = b.isLearned
                    when {
                        aIsLearned && !bIsLearned -> -1
                        bIsLearned && !aIsLearned -> 1
                        else -> a.material.level.compareTo(b.material.level)
                    }
                })
        }

    }
}
