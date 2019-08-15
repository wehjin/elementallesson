package com.rubyhuntersky.data.material

import com.rubyhuntersky.data.material.core.CourseMaterial

object Sem1CourseMaterial : CourseMaterial {
    override val title = "Semester 1"
    override val subtitle = "Chapter 10"
    override val lessons = Sem1Chap10CourseMaterial.lessons
}