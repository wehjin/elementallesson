package com.rubyhuntersky.data

import kotlinx.serialization.Serializable

@Serializable
data class CourseMaterial(
    val title: String,
    val subtitle: String? = null,
    val lessons: List<LessonMaterial>
)