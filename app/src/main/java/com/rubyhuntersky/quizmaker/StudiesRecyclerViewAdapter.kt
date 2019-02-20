package com.rubyhuntersky.quizmaker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class StudiesRecyclerViewAdapter : RecyclerView.Adapter<StudyViewHolder>() {

    private var adapterItems = mutableListOf<String>()

    fun bind(items: List<String>) {
        if (this.adapterItems != items) {
            adapterItems.clear()
            adapterItems.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return adapterItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        return StudyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        val item = adapterItems[position]
        holder.bind(item)
    }
}