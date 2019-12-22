package com.rubyhuntersky.data.v2

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.tomicOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LearnerTest {

    @Test
    internal fun addLearner() {
        val tomic = tomicWithPrefix("addLeaner")
        val name = "main"
        val learner = tomic.addLearner(name)
        assertEquals(name, learner[Learner.Name])
    }

    private fun tomicWithPrefix(prefix: String): Tomic {
        val dir = createTempDir(prefix, "learnerTest")
        val tomic = tomicOf(dir) { emptyList() }
        return tomic.also { println("Location: $dir") }
    }
}

