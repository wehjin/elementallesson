package com.rubyhuntersky.quizmaker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubyhuntersky.data.Challenge

class AnswersRecyclerViewAdapter : RecyclerView.Adapter<AnswerViewHolder>() {

    private var adapterItems = mutableListOf<Challenge>()
    private var sendFail: (Int) -> Unit = { _ -> Unit }

    fun bind(items: List<Challenge>, sendFail: (Int) -> Unit) {
        this.sendFail = sendFail
        if (this.adapterItems != items) {
            adapterItems.clear()
            adapterItems.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return adapterItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        return AnswerViewHolder(parent)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val item = adapterItems[position]
        holder.bind(item, onFail = { sendFail(position) })
    }
}