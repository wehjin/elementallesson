package com.rubyhuntersky.data.material

import com.rubyhuntersky.data.material.core.CourseMaterial

object Sem1CourseMaterial : CourseMaterial {
    override val title = "Semester 1"
    override val subtitle = "Chapters 1-10"
    override val lessons = Sem1Chap01CourseMaterial.lessons +
            Sem1Chap02CourseMaterial.lessons +
            Sem1Chap10CourseMaterial.lessons
}