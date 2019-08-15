package com.rubyhuntersky.data.material

interface DegreeMaterial {
    val courses: List<CourseMaterial>
}

object JapaneseDegreeMaterial : DegreeMaterial {
    override val courses: List<CourseMaterial> = listOf(
        Sem1Chap10CourseMaterial,
        Sem1Chap10CourseMaterial
    )
}