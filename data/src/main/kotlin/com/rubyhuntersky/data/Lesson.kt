package com.rubyhuntersky.data

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime


@Serializable
data class Lesson(
    val material: LessonMaterial,
    @Serializable(with = LocalDateTimeSerializer::class)
    val struggleTime: LocalDateTime = LocalDateTime.now() - Duration.ofMinutes(2),
    @Serializable(with = LocalDateTimeSerializer::class)
    val easyTime: LocalDateTime? = null
) {
    val wakeTime: LocalDateTime
        get() {
            return if (easyTime == null || easyTime < struggleTime) {
                struggleTime + Duration.ofMinutes(1)
            } else {
                val rested = Duration.between(struggleTime, easyTime) + Duration.ofHours(2)
                when {
                    rested > Duration.ofDays(32) -> easyTime + Duration.ofDays(64)
                    rested > Duration.ofDays(16) -> easyTime + Duration.ofDays(32)
                    rested > Duration.ofDays(8) -> easyTime + Duration.ofDays(16)
                    rested > Duration.ofDays(4) -> easyTime + Duration.ofDays(8)
                    rested > Duration.ofDays(2) -> easyTime + Duration.ofDays(4)
                    rested > Duration.ofDays(1) -> easyTime + Duration.ofDays(2)
                    else -> easyTime + Duration.ofDays(1)
                } - Duration.ofHours(2)
            }
        }

    fun setEasy(time: LocalDateTime) = copy(easyTime = time)
}