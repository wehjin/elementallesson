package com.rubyhuntersky.quizmaker.android

import java.time.Duration

fun Duration.toRelativeString(): String {
    return when {
        this >= Duration.ofDays(30) -> {
            when (val months = toDays() / 30) {
                1L -> "1 month"
                else -> "$months months"
            }
        }
        this >= Duration.ofDays(7) -> {
            val days = toDays()
            "${days / 7} weeks".let {
                val daysRemaining = when (val remainder = days % 7) {
                    0L -> ""
                    1L -> "1 day"
                    else -> "$remainder days"
                }
                "$it $daysRemaining"
            }
        }
        this >= Duration.ofDays(1) -> {
            when (val days = toDays()) {
                1L -> "1 day"
                else -> "$days days"
            }
        }
        this >= Duration.ofHours(1) -> "${toHours()} hours"
        else -> "${toMinutes()} minutes"
    }
}
