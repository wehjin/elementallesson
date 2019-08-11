package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.chapter10CourseMaterial
import java.time.LocalDateTime.now

class FirstStepFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {

        val course = Course.start(chapter10CourseMaterial, now())
        val activeLessons = course.toActiveLessons(now())

        return Guidance(
            course.title,
            course.subtitle,
            "Lessons: ${activeLessons.size}",
            activity!!.getDrawable(R.drawable.ic_launcher_background)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        val engage = GuidedAction.Builder(activity)
            .id(1)
            .title("Study")
            .hasNext(true)
            .build()
        actions.add(engage)
    }
}
