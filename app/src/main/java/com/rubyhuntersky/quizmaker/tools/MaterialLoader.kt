package com.rubyhuntersky.quizmaker.tools

import com.rubyhuntersky.data.material.Sem1CourseMaterial
import com.rubyhuntersky.data.material.Sem2CourseMaterial
import com.rubyhuntersky.data.material.core.CourseMaterial
import com.rubyhuntersky.data.material.core.DegreeMaterial
import com.rubyhuntersky.data.material.core.LessonMaterial
import com.rubyhuntersky.data.material.core.LessonType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.CoroutineContext

object MaterialLoader : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()
    private val okClient = OkHttpClient()
    private var localDegreeMaterial: DegreeMaterial? = null

    val basicDegreeMaterial: DegreeMaterial
        get() = localDegreeMaterial?.let { it }
            ?: runBlocking(Dispatchers.IO) {
                fetchBasicDegree()
            }.also {
                localDegreeMaterial = it
            }

    private fun fetchBasicDegree(): DegreeMaterial {
        return object : DegreeMaterial {
            override val courses = listOf(
                Sem1CourseMaterial,
                object : CourseMaterial {
                    override val title = Sem2CourseMaterial.title
                    override val subtitle = Sem2CourseMaterial.subtitle
                    override val lessons = (11..14)
                        .map(this@MaterialLoader::fetchChapterLessons)
                        .fold(
                            initial = Sem2CourseMaterial.lessons,
                            operation = { sum, more -> sum + more }
                        )
                },
                object : CourseMaterial {
                    override val title = "Chapter 14"
                    override val subtitle = "Semester 2"
                    override val lessons = fetchChapterLessons(14).toList()
                },
                object : CourseMaterial {
                    override val title = "Experimental"
                    override val subtitle = "Cached"
                    override val lessons = listOf(
                        LessonMaterial(
                            level = 99,
                            type = LessonType.LISTENING,
                            prompt = "atogaafterward",
                            promptColor = "後が",
                            response = "afterward"
                        ),
                        LessonMaterial(
                            level = 99,
                            type = LessonType.LISTENING,
                            prompt = "雨が",
                            response = "rain"
                        )
                    )
                }
            )
        }
    }

    private fun fetchChapterLessons(chapter: Int): Sequence<LessonMaterial> {
        val url = "https://wehjin.github.io/gan2/ch$chapter/LESSONS.LST"
        val request = Request.Builder().url(url).build()
        val response = okClient.newCall(request).execute()
        if (!response.isSuccessful) {
            error("$url: ${response.code} ${response.message}")
        }
        val lines = response.body!!.string().lineSequence()
        return lines.mapNotNull(this@MaterialLoader::getLessonMaterial)
    }

    private fun getLessonMaterial(line: String): LessonMaterial? {
        val parts = line.split(":")
        val lessonType = getLessonType(parts[0])
        return lessonType?.let {
            val promptParts = parts[1].trim().split("/")
            val responseParts = parts[2].trim().split("/")
            LessonMaterial(
                level = parts[3].toInt(),
                type = lessonType,
                prompt = promptParts[0],
                response = responseParts[0],
                promptColor = promptParts.getOrNull(1),
                responseColor = responseParts.getOrNull(1)
            )
        }
    }

    private fun getLessonType(string: String): LessonType? = when (string.trim()) {
        "produce" -> LessonType.PRODUCTION
        "listen" -> LessonType.LISTENING
        "cloze" -> LessonType.CLOZE
        "shadow" -> LessonType.SHADOW
        "" -> null
        else -> error("Invalid lesson type: $string")
    }
}