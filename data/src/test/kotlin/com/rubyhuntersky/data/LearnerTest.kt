package com.rubyhuntersky.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*


class LearnerTest {

    private val default = Learner()
    private val challenge = Challenge("2+2", "4")
    private val addQuiz = default.addQuiz(
        name = "my quiz",
        challenges = listOf(challenge),
        optionalId = "1"
    )

    @Test
    fun addPerformance() {
        val date = Date(1234567)
        val addPerformance = addQuiz.addPerformance("1", date)
        assertEquals(
            setOf(Performance(date, "1", "my quiz")),
            addPerformance.performances
        )
    }

    @Test
    fun findQuiz() {
        val quiz = addQuiz.findQuiz("1")
        assertEquals(Quiz("1", "my quiz", listOf(challenge)), quiz)
    }

    @Test
    fun addQuiz() {
        assertEquals(
            setOf(Quiz("1", "my quiz", listOf(challenge))),
            addQuiz.quizzes
        )
        assertEquals(
            setOf(Performance(null, "1", "my quiz")),
            addQuiz.performances
        )
    }

    @Test
    fun default() {
        assertEquals(emptySet<Quiz>(), default.quizzes)
        assertEquals(emptySet<Performance>(), default.performances)
    }
}