package com.rubyhuntersky.quizmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubyhuntersky.data.Challenge
import kotlinx.android.synthetic.main.view_answer.view.*

class AnswerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(createView(parent)) {

    fun bind(challenge: Challenge, onFail: () -> Unit) {
        itemView.questionTextView.text = challenge.question
        itemView.answerTextView.text = challenge.answer
        itemView.failButton.setOnClickListener { onFail() }
    }

    companion object {
        fun createView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.view_answer, parent, false)
        }
    }
}