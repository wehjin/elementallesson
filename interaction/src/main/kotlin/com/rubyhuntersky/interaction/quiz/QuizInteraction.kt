package com.rubyhuntersky.interaction.quiz

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.Learner
import com.rubyhuntersky.interaction.core.BehaviorInteraction
import com.rubyhuntersky.interaction.core.Book
import java.util.*

sealed class Vision {
    object Idle : Vision()
    data class Quizzing(val topics: List<String>) : Vision()
    data class Grading(val knownChallenges: List<Challenge>) : Vision()
    data class Learning(val unknownChallenges: List<Challenge>) : Vision()
    object Celebrating : Vision()
}

sealed class Action {
    object Quit : Action()
    data class Load(val quizId: String) : Action()
    data class AddAnswer(val index: Int, val isAnswerKnown: Boolean) : Action()
    data class FailAnswer(val index: Int) : Action()
    object FinishGrading : Action()
    data class Finish(val finishedOn: Date) : Action()
}

class QuizInteraction(private val learnerBook: Book<Learner>) :
    BehaviorInteraction<Vision, Action>(Vision.Idle, Action.Quit) {

    private lateinit var quizId: String
    private val unanswered = mutableListOf<Challenge>()
    private val known = mutableListOf<Challenge>()
    private val unknown = mutableListOf<Challenge>()

    override fun sendAction(action: Action) {
        when (action) {
            Action.Quit -> {
                setVision(Vision.Idle)
            }
            is Action.Load -> {
                quizId = action.quizId
                with(unanswered) {
                    clear()
                    addAll(learnerBook.value.findQuiz(quizId)!!.challenges)
                    repeat(5) { shuffle() }
                }
                known.clear()
                unknown.clear()
                if (unanswered.isEmpty()) {
                    setVision(Vision.Celebrating)
                } else {
                    setVisionToQuizzing()
                }
            }
            is Action.AddAnswer ->
                if (vision is Vision.Quizzing) {
                    (if (action.isAnswerKnown) known else unknown).add(unanswered.removeAt(action.index))
                    if (unanswered.isEmpty()) {
                        setVisionToGradingOrLearning()
                    } else {
                        setVisionToQuizzing()
                    }
                }
            is Action.FailAnswer ->
                if (vision is Vision.Grading) {
                    unknown.add(known.removeAt(action.index))
                    setVisionToGradingOrLearning()
                }
            Action.FinishGrading -> if (vision is Vision.Grading) {
                setVisionToLearning()
            }
            is Action.Finish -> if (vision is Vision.Learning || vision is Vision.Celebrating) {
                val newLearner = learnerBook.value.addPerformance(quizId, action.finishedOn)
                learnerBook.write(newLearner)
                setVision(Vision.Idle)
            }
        }
    }

    private fun setVisionToQuizzing() {
        setVision(Vision.Quizzing(unanswered.map(Challenge::question)))
    }

    private fun setVisionToGradingOrLearning() {
        if (known.isEmpty()) {
            setVisionToLearning()
        } else {
            setVision(Vision.Grading(known.toList()))
        }
    }

    private fun setVisionToLearning() {
        if (unknown.isEmpty()) {
            setVision(Vision.Celebrating)
        } else {
            repeat(5) { unknown.shuffle() }
            setVision(Vision.Learning(unknown.toList()))
        }
    }
}