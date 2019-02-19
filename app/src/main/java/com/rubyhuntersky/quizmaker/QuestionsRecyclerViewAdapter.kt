package com.rubyhuntersky.quizmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class QuestionsRecyclerViewAdapter : RecyclerView.Adapter<QuestionViewHolder>() {

    private var adapterQuestions = mutableListOf<String>()

    fun setQuestions(questions: List<String>) {
        if (adapterQuestions != questions) {
            adapterQuestions.clear()
            adapterQuestions.addAll(questions)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return adapterQuestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = adapterQuestions[position]
        holder.bind(question)
    }
}