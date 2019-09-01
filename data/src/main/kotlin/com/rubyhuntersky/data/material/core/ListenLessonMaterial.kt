package com.rubyhuntersky.data.material.core

import kotlinx.serialization.Serializable

@Serializable
data class ListenLessonMaterial(
    val level: Int,
    val foreignMediaPrompt: String,
    val nativeTextResponse: String,
    val foreignTextResponse: String
) {
    val id: String by lazy { "$level:$foreignMediaPrompt" }
}
