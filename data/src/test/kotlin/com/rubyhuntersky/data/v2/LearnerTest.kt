package com.rubyhuntersky.data.v2

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class LearnerTest {

    @Test
    internal fun createLearner() {
        val tomic = tomic("addLeaner", "learnerTest")
        val learner = tomic.createLearner()
        assertNotNull(learner)
    }
}

