package com.rubyhuntersky.quizmaker

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.interaction.quiz.Action
import com.rubyhuntersky.interaction.quiz.Vision
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.view_quiz.*
import java.util.*

class QuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_quiz)
        setupViews()
    }

    private fun setupViews() {
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
        with(studiesRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = StudiesRecyclerViewAdapter()
        }
        studiesDoneButton.setOnClickListener {
            sendAction(Action.Finish(Date()))
            finish()
        }
        celebratingRepeatButton.setOnClickListener {
            sendAction(Action.Finish(Date()))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        QuizPortal.interaction.visionStream.observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                Log.d(this.javaClass.simpleName, "VISION: $it")
            }
            .subscribe(this@QuizActivity::render) {
                Log.e(this.javaClass.simpleName, "Vision stream error", it)
            }.addTo(composite)
    }

    private fun sendAction(action: Action) {
        Log.d(this.javaClass.simpleName, "ACTION: $action")
        QuizPortal.interaction.sendAction(action)
    }

    private fun render(vision: Vision) {
        return when (vision) {
            Vision.Idle -> revealView("Idle", null)
            is Vision.Quizzing -> {
                val adapter = quizzingRecyclerView.adapter as QuestionsRecyclerViewAdapter
                adapter.bind(vision.topics) { index, result ->
                    sendAction(Action.AddAnswer(index, result))
                }
                revealView("Answer these questions", quizzingLinearLayout)
            }
            is Vision.Grading -> {
                val adapter = gradingRecyclerView.adapter as AnswersRecyclerViewAdapter
                adapter.bind(vision.knownChallenges) { index ->
                    sendAction(Action.FailAnswer(index))
                }
                revealView("Check your answers", gradingFrameLayout)
            }
            is Vision.Learning -> {
                val adapter = studiesRecyclerView.adapter as StudiesRecyclerViewAdapter
                adapter.bind(vision.unknownChallenges.map(Challenge::answer))
                revealView("Learn these answers", studiesLinearLayout)
            }
            is Vision.Celebrating -> revealView("You aced it!", celebratingFrameLayout)
        }
    }

    private fun revealView(pageTitle: String, view: View?) {
        title = pageTitle
        listOf(
            quizzingLinearLayout,
            gradingFrameLayout,
            studiesLinearLayout,
            celebratingFrameLayout
        ).forEach {
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
}
