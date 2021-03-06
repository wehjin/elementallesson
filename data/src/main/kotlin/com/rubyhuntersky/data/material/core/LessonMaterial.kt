package com.rubyhuntersky.data.material.core

import kotlinx.serialization.Serializable

@Serializable
data class LessonMaterial(
    val level: Int = 1,
    val type: LessonType = LessonType.PRODUCTION,
    val prompt: String,
    val response: String,
    val promptColor: String? = null,
    val responseColor: String? = null
) {
    val id: String by lazy { "$type:$level:$prompt" }
}