package com.rubyhuntersky.quizmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class QuestionsRecyclerViewAdapter : RecyclerView.Adapter<QuestionViewHolder>() {

    private var adapterItems = mutableListOf<String>()
    private var sendAnswer: (Int, Boolean) -> Unit = { _, _ -> Unit }

    fun bind(items: List<String>, sendAnswer: (Int, Boolean) -> Unit) {
        this.sendAnswer = sendAnswer
        if (adapterItems != items) {
            adapterItems.clear()
            adapterItems.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = adapterItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = adapterItems[position]
        holder.bind(question, { sendAnswer(position, true) }, { sendAnswer(position, false) })
    }
}