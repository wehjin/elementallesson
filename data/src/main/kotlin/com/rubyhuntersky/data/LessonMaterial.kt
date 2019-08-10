package com.rubyhuntersky.data

import kotlinx.serialization.Serializable

@Serializable
data class LessonMaterial(
    val prompt: String,
    val response: String,
    val promptColor: String? = null,
    val responseColor: String? = null
)