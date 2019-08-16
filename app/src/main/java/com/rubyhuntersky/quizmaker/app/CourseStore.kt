package com.rubyhuntersky.quizmaker.app

import com.rubyhuntersky.data.Course
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File

object CourseStore {

    private val json = Json(JsonConfiguration.Stable)

    fun write(course: Course, courseFile: File) {
        val text = json.stringify(Course.serializer(), course)
        courseFile.writeText(text)
    }

    fun read(courseFile: File): Course? {
        return try {
            json.parse(Course.serializer(), courseFile.readText())
        } catch (e: Exception) {
            null
        }
    }
}