package com.rubyhuntersky.interaction.selectquiz

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.rubyhuntersky.data.Learner
import com.rubyhuntersky.interaction.core.Book
import com.rubyhuntersky.interaction.core.Portal
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.junit.Test

class SelectQuizInteractionTest {

    private val learnerAddQuiz = Learner().addQuiz("Quiz", emptyList(), "quiz")
    private val learnerBook = object : Book<Learner> {
        private val behavior = BehaviorSubject.createDefault(learnerAddQuiz)
        override val reader: Observable<Learner> get() = behavior.distinctUntilChanged()
        override fun write(value: Learner) = behavior.onNext(value)
    }


    private val quizPortal = mock<Portal<String>>()

    @Test
    fun select() {
        SelectQuizInteraction(learnerBook, quizPortal)
            .apply {
                reset()
                sendAction(Action.Select(0))
            }
        verify(quizPortal).jump("quiz")
    }

    @Test
    fun reset() {
        val interaction = SelectQuizInteraction(learnerBook, quizPortal)
            .apply { reset() }

        interaction.visionStream.test()
            .assertValue(Vision.Selecting(listOf(QuizDisplay("Quiz", null))))
    }
}