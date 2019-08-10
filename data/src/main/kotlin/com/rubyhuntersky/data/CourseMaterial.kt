package com.rubyhuntersky.data

import kotlinx.serialization.Serializable

@Serializable
data class CourseMaterial(
    val name: String,
    val lessons: List<LessonMaterial>
)