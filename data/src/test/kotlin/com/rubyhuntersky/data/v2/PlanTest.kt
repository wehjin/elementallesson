package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class PlanTest {

    @Test
    internal fun crud() {
        val tomic = tomic("crud", "planTest")
        val learner: Long = 1000

        val created = tomic.createPlan(learner, "First Plan")
        assertEquals(learner, created[Plan.Author]!!.number)

        val read = tomic.readPlans(learner)
        assertEquals("First Plan", read.first()[Plan.Name])

        val updated = read.first().ent.let { rename ->
            tomic.updatePlan(learner, rename) { Plan.Name set "First Plan - Renamed" }
        }
        assertEquals("First Plan - Renamed", updated.first()[Plan.Name])

        val deleted = updated.first().ent.let { retiring ->
            tomic.deletePlan(learner, retiring)
        }
        assertNotNull(deleted)
        assertEquals(0, tomic.readPlans(learner).size)
    }
}

