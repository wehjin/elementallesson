package com.rubyhuntersky.data.material

interface CourseMaterial {
    val title: String
    val subtitle: String?
    val lessons: List<LessonMaterial>
}