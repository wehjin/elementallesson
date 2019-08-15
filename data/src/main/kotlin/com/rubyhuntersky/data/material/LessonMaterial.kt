package com.rubyhuntersky.data.material

import kotlinx.serialization.Serializable

@Serializable
data class LessonMaterial(
    val level: Int = 1,
    val prompt: String,
    val response: String,
    val promptColor: String? = null,
    val responseColor: String? = null
)