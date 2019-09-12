package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.core.LessonMaterial
import com.rubyhuntersky.data.material.core.LessonType
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime


@Serializable
data class Lesson(
    val material: LessonMaterial,
    @Serializable(with = LocalDateTimeSerializer::class)
    val hardTime: LocalDateTime = LocalDateTime.now() - Duration.ofMinutes(2),
    @Serializable(with = LocalDateTimeSerializer::class)
    val easyTime: LocalDateTime? = null
) {
    val id: String
        get() = material.id

    val type: LessonType
        get() = material.type

    val isLearned: Boolean
        get() = learnedTime != null

    val clipBase: String?
        get() = if (type == LessonType.LISTENING) material.prompt else null

    fun isAwake(time: LocalDateTime): Boolean {
        return !wakeTime.isAfter(time)
    }

    val prompt get() = material.prompt
    val promptColor get() = material.promptColor
    val response get() = material.response
    val responseColor get() = material.responseColor

    fun mergeInto(previous: Lesson?): Lesson = previous?.copy(material = this.material) ?: this

    fun setEasy(time: LocalDateTime): Lesson {
        return if (easyTime == null || easyTime.isBefore(hardTime)) {
            copy(easyTime = time, hardTime = time)
        } else {
            copy(easyTime = time)
        }
    }

    fun setHard(time: LocalDateTime): Lesson = copy(hardTime = time, easyTime = null)

    val learnedTime: LocalDateTime?
        get() = easyTime?.let { if (it.isAfter(hardTime)) it else null }

    fun restDurationWithEasy(easy: LocalDateTime): Duration {
        val hard = if (easyTime == null || easyTime.isBefore(hardTime)) easy else hardTime
        return if (easy.isBefore(hard)) {
            Duration.ZERO
        } else {
            val rested = Duration.between(hard, easy) + Duration.ofHours(1)
            when {
                rested > Duration.ofDays(64) -> Duration.ofDays(128)
                rested > Duration.ofDays(32) -> Duration.ofDays(64)
                rested > Duration.ofDays(16) -> Duration.ofDays(32)
                rested > Duration.ofDays(8) -> Duration.ofDays(16)
                rested > Duration.ofDays(4) -> Duration.ofDays(8)
                rested > Duration.ofDays(2) -> Duration.ofDays(4)
                rested > Duration.ofDays(1) -> Duration.ofDays(2)
                else -> Duration.ofDays(1)
            } - Duration.ofHours(1)
        }
    }

    val wakeTime: LocalDateTime
        get() {
            return if (easyTime == null || easyTime < hardTime) {
                hardTime
            } else {
                val restDuration = restDurationWithEasy(easyTime)
                easyTime + restDuration
            }
        }
}