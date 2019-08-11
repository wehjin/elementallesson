package com.rubyhuntersky.quizmaker

import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
open class StepFragment : GuidedStepSupportFragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Main + Job()

    val control: SendChannel<Msg>
        get() = channel

    sealed class Msg {

        data class SetView(
            val guidance: Guidance,
            val buttons: List<Button>,
            val events: SendChannel<String>?
        ) : Msg()

        data class SendButtonEvent(val index: Int) : Msg()
        internal data class SetViewEnabled(val isViewEnabled: Boolean) : Msg()
    }

    data class Button(
        val text: String,
        val event: String,
        val hasNext: Boolean,
        val subtext: String? = null
    )

    private val channel = Channel<Msg>()

    init {
        launch {
            var guidance = Guidance("", "", "", null)
            var buttons = listOf<Button>()
            var events: SendChannel<String>? = null
            var isViewEnabled = false
            while (!channel.isClosedForReceive) {
                when (val msg = channel.receive()) {
                    is Msg.SetView -> {
                        guidance = msg.guidance
                        buttons = msg.buttons
                        events = msg.events
                        render(isViewEnabled, msg.guidance, msg.buttons)
                    }
                    is Msg.SetViewEnabled -> {
                        isViewEnabled = msg.isViewEnabled
                        render(msg.isViewEnabled, guidance, buttons)
                    }
                    is Msg.SendButtonEvent -> {
                        buttons[msg.index].let { button ->
                            events?.offer(button.event)
                        }
                    }
                }
            }
        }
    }

    private fun render(isViewEnabled: Boolean, guidance: Guidance, buttons: List<Button>) {
        if (isViewEnabled) {
            view?.apply {
                findViewById<TextView>(R.id.guidance_title).text = guidance.title
                findViewById<TextView>(R.id.guidance_description).text = guidance.description
                findViewById<TextView>(R.id.guidance_breadcrumb).text = guidance.breadcrumb
                findViewById<ImageView>(R.id.guidance_icon).setImageDrawable(guidance.iconDrawable)
            }
            actions = buttons.mapIndexed { i, button ->
                GuidedAction.Builder(activity)
                    .id((i + 1).toLong())
                    .title(button.text.toUpperCase())
                    .description(button.subtext)
                    .hasNext(button.hasNext)
                    .build()
            }
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        launch { channel.send(Msg.SendButtonEvent(action.id.toInt() - 1)) }
    }

    override fun onStart() {
        super.onStart()
        launch { channel.send(Msg.SetViewEnabled(true)) }
    }

    override fun onStop() {
        super.onStop()
        launch { channel.send(Msg.SetViewEnabled(false)) }
    }


}
