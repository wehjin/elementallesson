package com.rubyhuntersky.data

import com.rubyhuntersky.data.material.core.LessonMaterial
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

    val isLearned: Boolean
        get() = learnedTime != null

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
        get() = easyTime?.let { easy -> if (easy.isAfter(hardTime)) easy else null }

    fun restDurationWithEasy(easy: LocalDateTime): Duration {
        return if (easy < hardTime) {
            Duration.ZERO
        } else {
            val rested = Duration.between(hardTime, easy) + Duration.ofHours(2)
            when {
                rested > Duration.ofDays(32) -> Duration.ofDays(64)
                rested > Duration.ofDays(16) -> Duration.ofDays(32)
                rested > Duration.ofDays(8) -> Duration.ofDays(16)
                rested > Duration.ofDays(4) -> Duration.ofDays(8)
                rested > Duration.ofDays(2) -> Duration.ofDays(4)
                rested > Duration.ofDays(1) -> Duration.ofDays(2)
                rested > Duration.ofHours(12) -> Duration.ofDays(1)
                else -> Duration.ofHours(12)
            } - Duration.ofHours(2)
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