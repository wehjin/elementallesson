package com.rubyhuntersky.quizmaker

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_question.view.*

class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(question: String) {
        itemView.questionTextView.text = question
    }
}