package com.rubyhuntersky.quizmaker

import com.rubyhuntersky.data.GanbarooPublisher
import com.rubyhuntersky.data.Learner
import com.rubyhuntersky.interaction.core.Book
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object LearnerBook : Book<Learner> {

    private val behavior = BehaviorSubject.createDefault(
        GanbarooPublisher.quizzes
            .fold(
                initial = Learner(),
                operation = { learner, quiz ->
                    learner.addQuiz(quiz.name, quiz.challenges)
                }
            )
    )
    override val reader: Observable<Learner>
        get() = behavior.distinctUntilChanged()

    override fun write(value: Learner) = behavior.onNext(value)
}