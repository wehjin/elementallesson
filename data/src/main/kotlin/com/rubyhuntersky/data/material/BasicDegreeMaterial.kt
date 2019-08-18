package com.rubyhuntersky.data.material

import com.rubyhuntersky.data.material.core.CourseMaterial
import com.rubyhuntersky.data.material.core.DegreeMaterial

object BasicDegreeMaterial : DegreeMaterial {
    override val courses: List<CourseMaterial> = listOf(
        Sem1CourseMaterial,
        Sem1Chap01CourseMaterial,
        Sem1Chap02CourseMaterial,
        Sem1Chap03CourseMaterial,
        Sem1Chap10CourseMaterial
    )
}