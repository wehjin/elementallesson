package com.rubyhuntersky.data

import java.time.Duration
import java.time.LocalDateTime

data class Lesson(
    val material: LessonMaterial,
    val hardDate: LocalDateTime = LocalDateTime.now() - Duration.ofMinutes(1),
    val easyDate: LocalDateTime? = null
) {
    val nextQuizDate: LocalDateTime
        get() {
            val easy = easyDate
            return if (easy == null || easy < hardDate) {
                hardDate + Duration.ofMinutes(1)
            } else {
                val simmer = Duration.between(hardDate, easy) + Duration.ofHours(2)
                when {
                    simmer > Duration.ofDays(32) -> easy + Duration.ofDays(64)
                    simmer > Duration.ofDays(16) -> easy + Duration.ofDays(32)
                    simmer > Duration.ofDays(8) -> easy + Duration.ofDays(16)
                    simmer > Duration.ofDays(4) -> easy + Duration.ofDays(8)
                    simmer > Duration.ofDays(2) -> easy + Duration.ofDays(4)
                    simmer > Duration.ofDays(1) -> easy + Duration.ofDays(2)
                    else -> easy + Duration.ofDays(1)
                } - Duration.ofHours(2)
            }
        }
}