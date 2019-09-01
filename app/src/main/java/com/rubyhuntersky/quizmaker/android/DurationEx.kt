package com.rubyhuntersky.quizmaker.android

import java.time.Duration

fun Duration.toRelativeString(): String {
    return when {
        this >= Duration.ofDays(30) -> "${toDays() / 30} months"
        this >= Duration.ofDays(7) -> "${toDays() / 7} weeks"
        this >= Duration.ofDays(1) -> "${toDays()} days"
        this >= Duration.ofHours(1) -> "${toHours()} hours"
        else -> "${toMinutes()} minutes"
    }
}
