package com.rubyhuntersky.quizmaker.android

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.VerticalGridPresenter
import com.rubyhuntersky.data.material.BasicDegreeMaterial
import com.rubyhuntersky.data.material.core.CourseMaterial
import com.rubyhuntersky.quizmaker.AppScope
import com.rubyhuntersky.quizmaker.CourseActivity
import com.rubyhuntersky.quizmaker.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class DegreeFragment : VerticalGridSupportFragment(), CoroutineScope, AppScope {
    override fun getApplication(): Application = activity!!.application
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = coursesAdapter
        title = "Courses"
        gridPresenter = VerticalGridPresenter().apply { numberOfColumns = 4 }

        setOnItemViewClickedListener { _, item, _, _ ->
            val courseMaterial = item as CourseMaterial
            val intent = Intent(context, CourseActivity::class.java)
            startActivity(intent)
        }
    }

    private val coursesAdapter: ArrayObjectAdapter by lazy {
        ArrayObjectAdapter(CourseCardPresenter()).apply {
            addAll(0, BasicDegreeMaterial.courses)
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