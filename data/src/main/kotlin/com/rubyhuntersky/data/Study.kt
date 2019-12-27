package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.core.DegreeMaterial
import com.rubyhuntersky.tomedb.Tomic
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class Study(val courses: List<Course>) {
    fun replaceCourse(course: Course): Study {
        val newCourses = courses.toMutableList()
            .also { list ->
                list.replaceAll { if (it.title == course.title) course else it }
            }
        return copy(courses = newCourses)
    }

    fun mergeInto(previous: Study?): Study {
        return previous?.let {
            val previousCourseTitles = previous.courses.map(Course::fullTitle).toSet()
            val newCourses = this.courses.filter { !previousCourseTitles.contains(it.fullTitle) }
            val updateMap = this.courses.associateBy(Course::fullTitle)
            val updatedCourses = previous.courses.mapNotNull { updateMap[it.fullTitle]?.mergeInto(it) }
            previous.copy(courses = updatedCourses + newCourses)
        } ?: this
    }

    companion object {
        fun start(degreeMaterial: DegreeMaterial, time: LocalDateTime): Study {
            val courses = degreeMaterial.courses.map { material ->
                Course(
                    title = material.title,
                    subtitle = material.subtitle,
                    lessons = material.lessons.map { Lesson(it, time - Duration.ofMinutes(2)) }.toSet()
                )
            }
            return Study(courses)
        }
    }
}

fun Tomic.createStudy(
    owner: Long,
    name: String
) {
}
