package com.rubyhuntersky.quizmaker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class QuizTitlesRecyclerViewAdapter : RecyclerView.Adapter<QuizTitleViewHolder>() {

    fun bind(titles: List<String>, onClick: (Int) -> Unit) {
        if (boundTitles.toList() != titles) {
            boundTitles.clear()
            boundTitles.addAll(titles)
            notifyDataSetChanged()
        }
        boundOnClick = onClick
    }

    private var boundTitles = mutableListOf<String>()
    private var boundOnClick = { _: Int -> Unit }

    override fun getItemCount(): Int =
        boundTitles.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizTitleViewHolder =
        QuizTitleViewHolder(parent)

    override fun onBindViewHolder(holder: QuizTitleViewHolder, position: Int) =
        holder.bind(boundTitles[position]) {
            boundOnClick(position)
        }
}