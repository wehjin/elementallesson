package com.rubyhuntersky.quizmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_quiz_title.view.*

class QuizTitleViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(createView(parent)) {

    fun bind(title: String, onClick: () -> Unit) {
        with(itemView.titleTextView) {
            text = title
            setOnClickListener {
                onClick()
            }
        }
    }

    companion object {
        fun createView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.view_quiz_title, parent, false)
        }
    }
}

