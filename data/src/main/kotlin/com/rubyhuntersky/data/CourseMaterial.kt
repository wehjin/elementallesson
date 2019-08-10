package com.rubyhuntersky.data

data class CourseMaterial(
    val name: String,
    val lessons: Set<LessonMaterial>
)