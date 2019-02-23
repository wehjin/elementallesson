package com.rubyhuntersky.quizmaker

import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubyhuntersky.interaction.selectquiz.Action
import com.rubyhuntersky.interaction.selectquiz.QuizDisplay
import com.rubyhuntersky.interaction.selectquiz.SelectQuizInteraction
import com.rubyhuntersky.interaction.selectquiz.Vision
import com.rubyhuntersky.quizmaker.android.Projector
import com.rubyhuntersky.quizmaker.android.ProjectorActivity
import com.rubyhuntersky.quizmaker.android.findViewByIdInLayout

class SelectQuizActivity : ProjectorActivity<Vision, Action>(
    Projector("SelectQuizActivity", interaction = SelectQuizInteraction(LearnerBook, QuizPortal).apply { reset() })
        .addComponent(object : Projector.Component<Vision, Vision.Reading, Action> {
            override fun convert(vision: Vision, activity: AppCompatActivity): Vision.Reading? =
                vision as? Vision.Reading

            override fun render(vision: Vision.Reading, sendAction: (Action) -> Unit, activity: AppCompatActivity) {
                activity.setContentView(Space(activity))
            }
        })
        .addComponent(object : Projector.Component<Vision, Vision.Selecting, Action> {
            override fun convert(vision: Vision, activity: AppCompatActivity): Vision.Selecting? =
                vision as? Vision.Selecting

            override fun render(vision: Vision.Selecting, sendAction: (Action) -> Unit, activity: AppCompatActivity) {
                val recyclerView = activity.findViewByIdInLayout<RecyclerView>(
                    viewId = R.id.selectQuizRecyclerView,
                    layoutId = R.layout.view_select_quiz_selecting
                ) {
                    it.layoutManager = LinearLayoutManager(activity)
                    it.adapter = QuizTitlesRecyclerViewAdapter()
                }
                (recyclerView.adapter as QuizTitlesRecyclerViewAdapter)
                    .bind(
                        titles = vision.quizzes.map(QuizDisplay::quizName),
                        onClick = { index -> sendAction(Action.Select(index)) }
                    )
            }
        })
)

