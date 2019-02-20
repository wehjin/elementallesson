package com.rubyhuntersky.quizmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_study.view.*

class StudyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(createView(parent)) {

    fun bind(study: String) {
        itemView.studyTextView.text = study
    }

    companion object {
        fun createView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.view_study, parent, false)
        }
    }
}