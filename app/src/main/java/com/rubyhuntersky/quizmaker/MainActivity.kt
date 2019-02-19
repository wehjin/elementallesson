package com.rubyhuntersky.quizmaker

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.Quiz
import com.rubyhuntersky.interaction.quiz.Action
import com.rubyhuntersky.interaction.quiz.QuizInteraction
import com.rubyhuntersky.interaction.quiz.Vision
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(quizzingRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = QuestionsRecyclerViewAdapter()
        }
        with(gradingRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = AnswersRecyclerViewAdapter()
        }
        gradingDoneButton.setOnClickListener {
            sendAction(Action.FinishGrading)
        }
        if (savedInstanceState == null) {
            sendAction(
                Action.Load(
                    Quiz(
                        listOf(
                            Challenge("4th day of the month", "yokka"),
                            Challenge("14th day of the month", "juuyokka")
                        )
                    )
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        interaction.visionStream.observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                Log.d(this.javaClass.simpleName, "VISION: $it")
            }
            .subscribe(this@MainActivity::render) {
                Log.e(this.javaClass.simpleName, "Vision stream error", it)
            }.addTo(composite)
    }

    private fun sendAction(action: Action) {
        Log.d(this.javaClass.simpleName, "ACTION: $action")
        interaction.sendAction(action)
    }

    private fun render(vision: Vision) {
        return when (vision) {
            Vision.Idle -> revealView("Idle", null)
            is Vision.Quizzing -> {
                val adapter = quizzingRecyclerView.adapter as QuestionsRecyclerViewAdapter
                adapter.bind(
                    items = vision.topics,
                    sendAnswer = { index, result -> sendAction(Action.AddAnswer(index, result)) }
                )
                revealView("Answer these questions", quizzingRecyclerView)
            }
            is Vision.Grading -> {
                val adapter = gradingRecyclerView.adapter as AnswersRecyclerViewAdapter
                adapter.bind(
                    items = vision.knownChallenges,
                    sendFail = { index -> sendAction(Action.FailAnswer(index)) }
                )
                revealView("Check your answers", gradingFrameLayout)
            }
            is Vision.Learning -> revealView("Find answers for these questions", null)
        }
    }

    private fun revealView(pageTitle: String, view: View?) {
        title = pageTitle
        val views = listOf(quizzingRecyclerView, gradingFrameLayout)
        views.forEach {
            if (it == view) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private val composite = CompositeDisposable()

    override fun onStop() {
        composite.clear()
        super.onStop()
    }

    companion object {
        private val interaction = QuizInteraction()
    }
}
