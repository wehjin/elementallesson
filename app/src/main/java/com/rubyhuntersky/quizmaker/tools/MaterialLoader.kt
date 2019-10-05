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
        val ch11RemoteLessons = fetchChapterLessons(11)
        val ch12RemoteLessons = fetchChapterLessons(12)
        val ch13RemoteLessons = fetchChapterLessons(13)
        return object : DegreeMaterial {
            override val courses = listOf(
                Sem1CourseMaterial,
                object : CourseMaterial {
                    override val title = Sem2CourseMaterial.title
                    override val subtitle = Sem2CourseMaterial.subtitle
                    override val lessons =
                        Sem2CourseMaterial.lessons + ch11RemoteLessons + ch12RemoteLessons + ch13RemoteLessons
                },
                object : CourseMaterial {
                    override val title = "Experimental"
                    override val subtitle = "Semester 2"
                    override val lessons = ch13RemoteLessons.toList()
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