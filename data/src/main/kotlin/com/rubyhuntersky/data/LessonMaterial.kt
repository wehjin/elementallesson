package com.rubyhuntersky.data

data class LessonMaterial(
    val prompt: String,
    val response: String,
    val promptColor: String? = null,
    val responseColor: String? = null
)