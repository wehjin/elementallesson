package com.rubyhuntersky.data.material.core

interface CourseMaterial {
    val title: String
    val subtitle: String?
    val lessons: List<LessonMaterial>
}