package com.rubyhuntersky.data

interface CourseMaterial {
    val title: String
    val subtitle: String?
    val lessons: List<LessonMaterial>
}