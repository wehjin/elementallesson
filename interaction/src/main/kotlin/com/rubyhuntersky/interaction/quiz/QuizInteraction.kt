package com.rubyhuntersky.interaction.quiz

import com.rubyhuntersky.data.*
import com.rubyhuntersky.interaction.core.BehaviorInteraction

sealed class Vision {
    object Idle : Vision()
    data class Picking(val titles: List<String>) : Vision()
    data class Quizzing(val topics: List<String>) : Vision()
    data class Grading(val knownChallenges: List<Challenge>) : Vision()
    data class Learning(val unknownChallenges: List<Challenge>) : Vision()
    object Celebrating : Vision()
}

sealed class Action {
    object Quit : Action()
    data class Load(val publisher: Publisher) : Action()
    data class SelectQuiz(val quizIndex: Int) : Action()
    data class AddAnswer(val index: Int, val isAnswerKnown: Boolean) : Action()
    data class FailAnswer(val index: Int) : Action()
    object FinishGrading : Action()
    object Reload : Action()
}

class QuizInteraction : BehaviorInteraction<Vision, Action>(Vision.Idle, Action.Quit) {

    private lateinit var publisher: Publisher
    private lateinit var selectedChallengeList: ChallengeList
    private val unanswered = mutableListOf<Challenge>()
    private val known = mutableListOf<Challenge>()
    private val unknown = mutableListOf<Challenge>()

    override fun sendAction(action: Action) {
        when (action) {
            Action.Quit ->
                setVision(Vision.Idle)
            is Action.Load -> {
                publisher = action.publisher
                setVision(Vision.Picking(publisher.quizzes.map(NamedQuiz::name)))
            }
            is Action.SelectQuiz ->
                if (vision is Vision.Picking) {
                    selectedChallengeList = publisher.quizzes[action.quizIndex].toQuiz()
                    with(unanswered) {
                        clear()
                        addAll(selectedChallengeList.challenges)
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
            Action.FinishGrading ->
                if (vision is Vision.Grading) {
                    setVisionToLearning()
                }
            Action.Reload ->
                sendAction(Action.Load(publisher))
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