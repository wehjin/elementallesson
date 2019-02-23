package com.rubyhuntersky.interaction.selectquiz

import com.rubyhuntersky.data.Learner
import com.rubyhuntersky.data.Performance
import com.rubyhuntersky.interaction.core.BehaviorInteraction
import com.rubyhuntersky.interaction.core.Book
import com.rubyhuntersky.interaction.core.Portal
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*

sealed class Vision {
    object Reading : Vision()
    data class Selecting(val quizzes: List<QuizDisplay>) : Vision()
}

data class QuizDisplay(
    val quizName: String,
    val performedOn: Date?
)

sealed class Action {
    object Read : Action()
    data class Select(val index: Int) : Action()
}

class SelectQuizInteraction(
    private val learnerBook: Book<Learner>,
    private val quizPortal: Portal<String>
) : BehaviorInteraction<Vision, Action>(startVision = Vision.Reading, startAction = Action.Read) {

    override fun sendAction(action: Action) {
        when (action) {
            is Action.Read -> setVisionToReading()
            is Action.Select -> (vision as? Vision.Selecting)?.let {
                quizPortal.jump(performances[action.index].quizId)
            }
        }
    }

    private fun setVisionToReading() {
        setVision(Vision.Reading)
        composite.clear()
        learnerBook.reader
            .subscribe(this::setVisionToSelecting, this::printError)
            .addTo(composite)
    }

    private fun setVisionToSelecting(learner: Learner) {
        println("LEARNER: $learner")
        performances = learner.performances.sortedBy { it.performedOn }
        setVision(Vision.Selecting(performances.map {
            QuizDisplay(
                it.quizName,
                it.performedOn
            )
        }))
    }

    private fun printError(it: Throwable) {
        println(it.localizedMessage)
    }

    private val composite = CompositeDisposable()
    private lateinit var performances: List<Performance>
}