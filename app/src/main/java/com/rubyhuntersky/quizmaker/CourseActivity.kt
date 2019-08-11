package com.rubyhuntersky.quizmaker

import android.R
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import com.rubyhuntersky.data.Course
import com.rubyhuntersky.data.chapter10CourseMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@ExperimentalCoroutinesApi
class CourseActivity : FragmentActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val story = Costory(ConflatedBroadcastChannel<ViewCourseMdl>(), Channel<ViewCourseMsg>())
        launch {
            var mdl = ViewCourseMdl(Course.start(chapter10CourseMaterial, LocalDateTime.now()))
            story.mdls.send(mdl)
            for (msg in story.msgs) {
                mdl = when (msg) {
                    is ViewCourseMsg.Quit -> mdl.also { story.msgs.close() }
                }.also {
                    story.mdls.send(it)
                }
            }
            story.mdls.close()
        }
        launch {
            val mdls = story.mdls.openSubscription()
            val actionChannelId = Random.nextLong()
            val actionChannel = Channel<Any>().also { ChannelLookup[actionChannelId] = it }
            while (!isDestroyed && !mdls.isClosedForReceive) {
                select<Unit> {
                    mdls.onReceive { renderCourse(it.course, actionChannelId) }
                    actionChannel.onReceive { action ->
                        (action as? Long)?.let { actionCode ->
                            val msg = if (actionCode == FirstStepFragment.Action.NO_GO) ViewCourseMsg.Quit else null
                            msg?.let { story.msgs.send(it) }
                        }
                    }
                }
            }
            ChannelLookup[actionChannelId] = null
            actionChannel.close()
            dropRenderings()
        }
    }

    private fun renderCourse(course: Course, actionChannelId: Long) {
        dropRenderings()
        val fragment = FirstStepFragment.build(
            title = course.title,
            subtitle = course.subtitle,
            count = course.getActiveLessons(LocalDateTime.now()).size,
            actionChannelId = actionChannelId
        )
        GuidedStepSupportFragment.addAsRoot(this@CourseActivity, fragment, R.id.content)
    }

    private fun dropRenderings() {
        GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
            ?.finishGuidedStepSupportFragments()
    }
}
