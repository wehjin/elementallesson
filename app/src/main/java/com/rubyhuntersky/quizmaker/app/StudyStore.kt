package com.rubyhuntersky.quizmaker.app

import com.rubyhuntersky.data.Study
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File

object StudyStore {

    private val json = Json(JsonConfiguration.Stable)

    fun write(study: Study, courseFile: File) {
        val text = json.stringify(Study.serializer(), study)
        courseFile.writeText(text)
    }

    fun read(studyFile: File): Study? {
        return try {
            json.parse(Study.serializer(), studyFile.readText())
        } catch (e: Exception) {
            null
        }
    }
}