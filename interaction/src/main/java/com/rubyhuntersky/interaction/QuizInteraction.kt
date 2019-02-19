package com.rubyhuntersky.interaction

import com.rubyhuntersky.data.Quiz
import com.rubyhuntersky.data.QuizQuestion
import com.rubyhuntersky.interaction.core.BehaviorInteraction

sealed class Vision {
    object Idle : Vision()
    data class Quizzing(val topics: List<String>) : Vision()
    data class Grading(val knownQuizQuestions: List<QuizQuestion>) : Vision()
    data class Learning(val unknownQuizQuestions: List<QuizQuestion>) : Vision()
}

sealed class Action {
    object Quit : Action()
    data class Load(val quiz: Quiz) : Action()
    data class AnswerQuestion(val index: Int, val isAnswerKnown: Boolean) : Action()
    data class FailedGrading(val index: Int) : Action()
    object FinishGrading : Action()
    object Reload : Action()
}

class QuizInteraction : BehaviorInteraction<Vision, Action>(Vision.Idle, Action.Quit) {

    private lateinit var quiz: Quiz
    private val unanswered = mutableListOf<QuizQuestion>()
    private val known = mutableListOf<QuizQuestion>()
    private val unknown = mutableListOf<QuizQuestion>()

    override fun sendAction(action: Action) {
        when (action) {
            Action.Quit -> setVision(Vision.Idle)
            is Action.Load -> {
                quiz = action.quiz
                with(unanswered) { clear(); addAll(quiz.questions) }
                known.clear()
                unknown.clear()
                if (unanswered.isEmpty()) {
                    setVision(Vision.Idle)
                } else {
                    setVisionToQuizzing()
                }
            }
            is Action.AnswerQuestion -> {
                (if (action.isAnswerKnown) known else unknown).add(unanswered.removeAt(action.index))
                if (unanswered.isEmpty()) {
                    setVisionToGradingOrLearning()
                } else {
                    setVisionToQuizzing()
                }
            }
            is Action.FailedGrading -> {
                unknown.add(known.removeAt(action.index))
                setVisionToGradingOrLearning()
            }
            Action.FinishGrading -> setVisionToLearning()
            Action.Reload -> sendAction(Action.Load(quiz))
        }
    }

    private fun setVisionToQuizzing() = setVision(Vision.Quizzing(unanswered.map(QuizQuestion::prompt)))

    private fun setVisionToGradingOrLearning() {
        if (known.isEmpty()) {
            setVisionToLearning()
        } else {
            setVision(Vision.Grading(known))
        }
    }

    private fun setVisionToLearning() = setVision(Vision.Learning(unknown))
}