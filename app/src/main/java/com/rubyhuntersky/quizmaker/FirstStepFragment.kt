package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class FirstStepFragment : GuidedStepSupportFragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Main + Job()

    private lateinit var actionChannel: Channel<Any>

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val channelId = arguments!!.getLong(CHANNEL_ID)
        actionChannel = ChannelLookup[channelId]!!

        val title = arguments!!.getString(TITLE)!!
        val subtitle = arguments!!.getString(SUBTITLE)
        val count = arguments!!.getInt(COUNT)
        val crumb = "Lessons: $count"
        val icon = activity!!.getDrawable(R.drawable.ic_launcher_background)
        return Guidance(title, subtitle, crumb, icon)
    }

    object Action {
        const val GO = 1L
        const val NO_GO = 2L
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.add(
            GuidedAction.Builder(activity)
                .id(Action.GO)
                .title("Study")
                .hasNext(true)
                .build()
        )
        actions.add(
            GuidedAction.Builder(activity)
                .id(Action.NO_GO)
                .title("Exit")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        launch { actionChannel.send(action.id) }
    }

    companion object {

        private const val TITLE = "title"
        private const val SUBTITLE = "subtitle"
        private const val COUNT = "count"
        private const val CHANNEL_ID = "channel"

        fun build(title: String, subtitle: String?, count: Int, actionChannelId: Long): FirstStepFragment {
            return FirstStepFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(TITLE, title)
                        putString(SUBTITLE, subtitle)
                        putInt(COUNT, count)
                        putLong(CHANNEL_ID, actionChannelId)
                    }
                }
        }
    }
}
