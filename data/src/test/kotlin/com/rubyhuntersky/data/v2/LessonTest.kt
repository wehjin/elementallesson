package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.reformEnt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LessonTest {

    @Test
    internal fun crud() {
        val tomic = tomic("crud", "lessonTest")
        val plan: Long = 2000

        val create = tomic.createPlanLesson(plan, "Hello", "Goodbye", 1)
        assertEquals(plan, create[Lesson.Plan]!!.number)

        val read = tomic.readPlanLessons(plan)
        assertEquals("Hello", read.first()[Lesson.Prompt])

        val update = read.first().ent.let { reprompting ->
            tomic.updatePlanLessons(plan) {
                reformEnt(reprompting) { Lesson.Prompt set "Hello Revisited" }
            }
        }
        assertEquals("Hello Revisited", update.first()[Lesson.Prompt])

        val delete = update.first().ent.let { retiring ->
            tomic.deletePlanLesson(plan, retiring)
        }
        assertEquals(0, delete.size)
    }
}

