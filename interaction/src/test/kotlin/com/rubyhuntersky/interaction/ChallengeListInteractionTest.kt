package com.rubyhuntersky.interaction

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.NamedQuiz
import com.rubyhuntersky.data.Publisher
import com.rubyhuntersky.interaction.quiz.Action
import com.rubyhuntersky.interaction.quiz.QuizInteraction
import com.rubyhuntersky.interaction.quiz.Vision
import org.junit.Test

class ChallengeListInteractionTest {

    private val a = Challenge("a", "A")
    private val b = Challenge("b", "B")
    private val quizGroup = object : Publisher {
        override val name: String = "QuizGroup"
        override val quizzes: List<NamedQuiz> = listOf(NamedQuiz("Quiz1", listOf(a, b)))
    }

    @Test
    fun happy() {
        val interaction = QuizInteraction()
        interaction.visionStream.test().assertValue(
            Vision.Idle
        )

        interaction.sendAction(Action.Load(quizGroup))
        interaction.visionStream.test().assertValue(
            Vision.Picking(quizGroup.quizzes.map(NamedQuiz::name))
        )

        interaction.sendAction(Action.SelectQuiz(0))
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
    }
}