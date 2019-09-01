package com.rubyhuntersky.data.material

import com.rubyhuntersky.data.material.core.CourseMaterial
import com.rubyhuntersky.data.material.core.LessonMaterial
import com.rubyhuntersky.data.material.core.LessonType

object ExperimentalCourseMaterial : CourseMaterial {
    override val title = "Experimental"
    override val subtitle = "Listening Lessons"
    override val lessons = listOf(
        LessonMaterial(
            level = 0,
            type = LessonType.LISTENING,
            prompt = "kaizokuou",
            response = "king of the pirates",
            responseColor = "海賊王"
        )
    )
}
