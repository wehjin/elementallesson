package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.reformEnt
import com.rubyhuntersky.tomedb.tomicOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class LearnerTest {

    @Test
    internal fun createLearner() {
        val tomic = tomicWithPrefix("addLeaner")
        val learner = tomic.createLearner()
        assertNotNull(learner)
    }

    @Test
    internal fun crudPlan() {
        val tomic = tomicWithPrefix("createPlan")
        val learner: Long = 1000

        val create = tomic.createPlan(learner, "First Plan")
        assertEquals(learner, create[Plan.Author]!!.number)

        val read = tomic.readPlans(learner)
        assertEquals("First Plan", read.first()[Plan.Name])

        val update = read.first().ent
            .let { renaming ->
                tomic.updatePlans(learner) { reformEnt(renaming) { Plan.Name set "First Plan - Renamed" } }
            }
        assertEquals("First Plan - Renamed", update.first()[Plan.Name])

        val delete = update.first().ent
            .let { retiring ->
                tomic.deletePlan(learner, retiring)
            }
        assertEquals(0, delete.size)
    }

    private fun tomicWithPrefix(prefix: String): Tomic {
        val dir = createTempDir(prefix, "learnerTest")
        val tomic = tomicOf(dir) { emptyList() }
        return tomic.also { println("Location: $dir") }
    }
}

