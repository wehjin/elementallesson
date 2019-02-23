package com.rubyhuntersky.interaction

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.Learner
import com.rubyhuntersky.data.Performance
import com.rubyhuntersky.interaction.core.BehaviorBook
import com.rubyhuntersky.interaction.quiz.Action
import com.rubyhuntersky.interaction.quiz.QuizInteraction
import com.rubyhuntersky.interaction.quiz.Vision
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class QuizInteractionTest {

    private val a = Challenge("a", "A")
    private val b = Challenge("b", "B")
    private val learnerBook =
        BehaviorBook(Learner().addQuiz("Quiz", listOf(a, b), "quiz"))


    @Test
    fun happy() {
        val interaction = QuizInteraction(learnerBook)
        interaction.visionStream.test().assertValue(
            Vision.Idle
        )
        interaction.sendAction(Action.Load("quiz"))
        interaction.visionStream.test().assertValue {
            it is Vision.Quizzing && it.topics.toSet() == setOf("a", "b")
        }

        interaction.sendAction(Action.AddAnswer(0, true))
        interaction.sendAction(Action.AddAnswer(0, false))
        interaction.visionStream.test().assertValue {
            it is Vision.Grading && it.knownChallenges.size == 1
        }

        interaction.sendAction(Action.FinishGrading)
        interaction.visionStream.test().assertValue {
            it is Vision.Learning && it.unknownChallenges.size == 1
        }

        val now = Date(12345667)
        interaction.sendAction(Action.Finish(now))
        interaction.visionStream.test().assertValue {
            it is Vision.Idle
        }
        assertEquals(
            Performance(now, "quiz", "Quiz"),
            learnerBook.value.performances.first()
        )
    }
}