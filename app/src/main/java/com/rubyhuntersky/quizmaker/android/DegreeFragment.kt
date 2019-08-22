package com.rubyhuntersky.quizmaker.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.VerticalGridPresenter
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.quizmaker.CourseActivity
import com.rubyhuntersky.quizmaker.LegendScope
import com.rubyhuntersky.quizmaker.R
import com.rubyhuntersky.quizmaker.app.AppMdl
import com.rubyhuntersky.quizmaker.app.AppMsg
import com.rubyhuntersky.quizmaker.app.AppScope
import com.rubyhuntersky.quizmaker.findLegend
import kotlinx.coroutines.*
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class DegreeFragment : VerticalGridSupportFragment(), CoroutineScope, AppScope, LegendScope {
    override fun getApplication(): Application = activity!!.application
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val appLegend by lazy { findLegend<AppMdl, AppMsg>()!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Courses"
        gridPresenter = VerticalGridPresenter().apply { numberOfColumns = 4 }

        setOnItemViewClickedListener { _, item, _, _ ->
            val course = item as Course
            Log.d("DegreeFragment", "Selected course: ${course.title}")
            launch {
                appLegend.send(AppMsg.SelectCourse(course))
                startActivity(Intent(context, CourseActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val mdls = appLegend.startMdls()
        launch {
            while (!mdls.isClosedForReceive) {
                when (val mdl = mdls.receive()) {
                    is AppMdl.ActiveStudy -> {
                        adapter = ArrayObjectAdapter(CourseCardPresenter()).apply { addAll(0, mdl.study.courses) }
                    }
                }
            }
        }

    }

    class CourseCardPresenter : Presenter() {

        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
            object : ViewHolder((LayoutInflater.from(parent.context).inflate(
                R.layout.view_course_card,
                parent,
                false
            ) as ImageCardView).also {
                it.focusable = View.FOCUSABLE
                it.isFocusableInTouchMode = true
                it.setMainImageDimensions(
                    it.resources.getDimensionPixelSize(R.dimen.lb_basic_card_main_height),
                    it.resources.getDimensionPixelSize(R.dimen.lb_basic_card_main_width)
                )
            }) {}

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val course = item as Course
            val imageCardView = viewHolder.view as ImageCardView
            imageCardView.titleText = course.title
            imageCardView.contentText = course.subtitle
            imageCardView.badgeImage = course.toBadge(viewHolder.view.context)
            imageCardView.mainImage = imageCardView.context.getDrawable(R.mipmap.ic_launcher)
        }

        private fun Course.toBadge(context: Context): Drawable? {
            return if (activeLessons(LocalDateTime.now()).isEmpty()) null else context.getDrawable(R.drawable.ic_whatshot_white_24dp)
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }
}