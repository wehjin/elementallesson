package com.rubyhuntersky.quizmaker.android

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.rubyhuntersky.data.material.CourseMaterial
import com.rubyhuntersky.data.material.JapaneseDegreeMaterial
import com.rubyhuntersky.quizmaker.AppScope
import com.rubyhuntersky.quizmaker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class DegreeBrowseFragment : BrowseSupportFragment(), CoroutineScope, AppScope {
    override fun getApplication(): Application = activity!!.application
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = "Japanese"
        adapter = categoryRowAdapter
        selectedPosition = 0
    }

    private val categoryRowAdapter: ArrayObjectAdapter by lazy {
        ArrayObjectAdapter(ListRowPresenter()).apply {
            add(ListRow(HeaderItem("Courses"), coursesAdapter))
        }
    }

    private val coursesAdapter: ArrayObjectAdapter by lazy {
        ArrayObjectAdapter(CourseCardPresenter()).apply {
            addAll(0, JapaneseDegreeMaterial.courses)
        }
    }


    class CourseCardPresenter : Presenter() {

        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            return object : ViewHolder((LayoutInflater.from(parent.context).inflate(
                R.layout.view_course_card,
                parent,
                false
            ) as ImageCardView)
                .also {
                    it.focusable = View.FOCUSABLE
                    it.isFocusableInTouchMode = true
                    it.setMainImageDimensions(
                        it.resources.getDimensionPixelSize(R.dimen.lb_basic_card_main_height),
                        it.resources.getDimensionPixelSize(R.dimen.lb_basic_card_main_width)
                    )
                }) {}
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val courseMaterial = item as CourseMaterial
            val imageCardView = viewHolder.view as ImageCardView
            imageCardView.titleText = courseMaterial.title
            imageCardView.contentText = courseMaterial.subtitle ?: ""
            imageCardView.mainImage = imageCardView.context.getDrawable(R.mipmap.ic_launcher)
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }
}