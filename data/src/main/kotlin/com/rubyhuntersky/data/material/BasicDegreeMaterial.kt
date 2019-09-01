package com.rubyhuntersky.data.material

import com.rubyhuntersky.data.material.core.DegreeMaterial

object BasicDegreeMaterial : DegreeMaterial {
    override val courses = listOf(
        Sem1CourseMaterial,
        Sem2CourseMaterial,
        ExperimentalCourseMaterial
    )
}