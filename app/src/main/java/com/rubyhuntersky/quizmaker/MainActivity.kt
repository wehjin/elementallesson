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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            with(quizzingRecyclerView) {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = QuestionsRecyclerViewAdapter()
            }
            sendAction(
                Action.Load(
                    Quiz(
                        listOf(
                            Challenge("Day 4", "yokka"),
                            Challenge("Day 14", "juuyokka")
                        )
                    )
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        interaction.visionStream
            .doOnNext {
                Log.d(this.javaClass.simpleName, "VISION: $it")
            }
            .subscribe(this@MainActivity::render).addTo(composite)
    }

    private fun sendAction(action: Action) {
        Log.d(this.javaClass.simpleName, "ACTION: $action")
        interaction.sendAction(action)
    }

    private fun render(vision: Vision) = when (vision) {
        Vision.Idle -> revealView(null)
        is Vision.Quizzing -> {
            val adapter = quizzingRecyclerView.adapter as QuestionsRecyclerViewAdapter
            adapter.bind(
                questions = vision.topics,
                sendAnswer = { index, result ->
                    sendAction(Action.AddAnswer(index, result))
                }
            )
            revealView(quizzingRecyclerView)
        }
        is Vision.Grading -> revealView(null)
        is Vision.Learning -> revealView(null)
    }

    private fun revealView(view: View?) {
        val views = listOf(quizzingRecyclerView as View)
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
