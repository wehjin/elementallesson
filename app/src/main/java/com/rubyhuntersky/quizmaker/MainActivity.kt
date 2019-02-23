package com.rubyhuntersky.quizmaker

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.GanbarooPublisher
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
        setupViews()
        if (savedInstanceState == null) {
            sendAction(Action.Load(GanbarooPublisher))
        }
    }

    private fun setupViews() {
        with(pickingRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = QuizTitlesRecyclerViewAdapter()
        }
        with(quizzingRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = QuestionsRecyclerViewAdapter()
        }
        quizzingRestartButton.setOnClickListener {
            sendAction(Action.Reload)
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
            sendAction(Action.Reload)
        }
        celebratingRepeatButton.setOnClickListener {
            sendAction(Action.Reload)
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
            is Vision.Picking -> {
                val adapter = pickingRecyclerView.adapter as QuizTitlesRecyclerViewAdapter
                adapter.bind(vision.titles) { index ->
                    sendAction(Action.SelectQuiz(index))
                }
                revealView("Select a quiz", pickingRecyclerView)
            }
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
            pickingRecyclerView,
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

    companion object {
        private val interaction = QuizInteraction()
    }
}
