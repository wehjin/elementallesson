package com.rubyhuntersky.interaction.quiz

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.Quiz
import com.rubyhuntersky.interaction.core.BehaviorInteraction

sealed class Vision {
    object Idle : Vision()
    data class Quizzing(val topics: List<String>) : Vision()
    data class Grading(val knownChallenges: List<Challenge>) : Vision()
    data class Learning(val unknownChallenges: List<Challenge>) : Vision()
}

sealed class Action {
    object Quit : Action()
    data class Load(val quiz: Quiz) : Action()
    data class AnswerChallenge(val index: Int, val isAnswerKnown: Boolean) : Action()
    data class FailAnswer(val index: Int) : Action()
    object FinishGrading : Action()
    object Reload : Action()
}

class QuizInteraction : BehaviorInteraction<Vision, Action>(
    Vision.Idle,
    Action.Quit
) {

    private lateinit var quiz: Quiz
    private val unanswered = mutableListOf<Challenge>()
    private val known = mutableListOf<Challenge>()
    private val unknown = mutableListOf<Challenge>()

    override fun sendAction(action: Action) {
        when (action) {
            Action.Quit -> setVision(Vision.Idle)
            is Action.Load -> {
                quiz = action.quiz
                with(unanswered) { clear(); addAll(quiz.challenges) }
                known.clear()
                unknown.clear()
                if (unanswered.isEmpty()) {
                    setVision(Vision.Idle)
                } else {
                    setVisionToQuizzing()
                }
            }
            is Action.AnswerChallenge -> {
                (if (action.isAnswerKnown) known else unknown).add(unanswered.removeAt(action.index))
                if (unanswered.isEmpty()) {
                    setVisionToGradingOrLearning()
                } else {
                    setVisionToQuizzing()
                }
            }
            is Action.FailAnswer -> {
                unknown.add(known.removeAt(action.index))
                setVisionToGradingOrLearning()
            }
            Action.FinishGrading -> setVisionToLearning()
            Action.Reload -> sendAction(
                Action.Load(
                    quiz
                )
            )
        }
    }

    private fun setVisionToQuizzing() = setVision(
        Vision.Quizzing(
            unanswered.map(
                Challenge::question
            )
        )
    )

    private fun setVisionToGradingOrLearning() {
        if (known.isEmpty()) {
            setVisionToLearning()
        } else {
            setVision(Vision.Grading(known))
        }
    }

    private fun setVisionToLearning() = setVision(Vision.Learning(unknown))
}