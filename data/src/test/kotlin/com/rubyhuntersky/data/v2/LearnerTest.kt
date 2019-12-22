package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.tomicOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LearnerTest {

    @Test
    internal fun createLearner() {
        val tomic = tomicWithPrefix("addLeaner")
        val name = "main"
        val learner = tomic.createLearner(name)
        assertEquals(name, learner[Learner.Name])
    }

    @Test
    internal fun createPlan() {
        val tomic = tomicWithPrefix("createPlan")
        val name = "First Plan"
        val plan = tomic.createPlan(1000, name)
        assertEquals(name, plan[Plan.Name])
    }

    private fun tomicWithPrefix(prefix: String): Tomic {
        val dir = createTempDir(prefix, "learnerTest")
        val tomic = tomicOf(dir) { emptyList() }
        return tomic.also { println("Location: $dir") }
    }
}

