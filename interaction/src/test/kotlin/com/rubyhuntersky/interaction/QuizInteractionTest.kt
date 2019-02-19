package com.rubyhuntersky.interaction

import com.rubyhuntersky.data.Quiz
import com.rubyhuntersky.data.Challenge
import org.junit.Test

class QuizInteractionTest {

    @Test
    fun happy() {
        val interaction = QuizInteraction()
        interaction.visionStream.test().assertValue(Vision.Idle)

        val a = Challenge("a", "A")
        val b = Challenge("b", "B")
        val quiz = Quiz(listOf(a, b))
        interaction.sendAction(Action.Load(quiz))
        interaction.visionStream.test().assertValue(Vision.Quizzing(listOf("a", "b")))

        interaction.sendAction(Action.AnswerQuestion(0, true))
        interaction.sendAction(Action.AnswerQuestion(0, false))
        interaction.visionStream.test().assertValue(Vision.Grading(listOf(a)))

        interaction.sendAction(Action.FinishGrading)
        interaction.visionStream.test().assertValue(Vision.Learning(listOf(b)))
    }
}