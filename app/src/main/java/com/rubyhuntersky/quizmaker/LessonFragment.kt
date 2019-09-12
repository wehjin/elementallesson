package com.rubyhuntersky.quizmaker

import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.data.material.core.LessonType
import com.rubyhuntersky.quizmaker.android.toRelativeString
import com.rubyhuntersky.quizmaker.tools.upgradeEllipsis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.time.Duration
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class LessonFragment : StepFragment() {

    suspend fun setSight(lesson: Lesson, events: Channel<String>) {
        val standardButtons = listOf(
            Button(
                "Check My Answer",
                event = CHECK_ANSWER,
                hasNext = true,
                subtext = "Try writing or saying it in a sentence."
            ),
            Button(
                "Back",
                event = CANCEL_LESSON,
                hasNext = false
            )
        )
        control.send(
            Msg.SetView(
                guidance = lesson.toGuidance(),
                buttons = lesson.toButtons(standardButtons),
                events = events
            )
        )
    }

    private fun Lesson.toButtons(standardButtons: List<Button>): List<Button> {
        return when (material.type) {
            LessonType.LISTENING -> {
                val playButton = Button(
                    "Play",
                    event = PLAY_CLIP,
                    hasNext = false
                )
                listOf(playButton) + standardButtons
            }
            else -> standardButtons
        }
    }

    private fun Lesson.toGuidance(): GuidanceStylist.Guidance {
        val breadcrumb = learnedTime?.let {
            "Last seen: ${Duration.between(
                it,
                LocalDateTime.now()
            ).toRelativeString()} ago"
        } ?: ""
        return when (material.type) {
            LessonType.PRODUCTION -> {
                val title = prompt
                val description = promptColor ?: ""
                GuidanceStylist.Guidance(title, description, breadcrumb, null)
            }
            LessonType.LISTENING -> {
                val title = "〚 Listen 〛"
                val description = ""
                GuidanceStylist.Guidance(title, description, breadcrumb, null)
            }
            LessonType.CLOZE -> {
                val title = "〚 Cloze 〛"
                val description = prompt.upgradeEllipsis()
                GuidanceStylist.Guidance(title, description, breadcrumb, null)
            }
            else -> TODO()
        }
    }

    companion object {
        const val CHECK_ANSWER = "checkAnswer"
        const val CANCEL_LESSON = "cancelLesson"
        const val PLAY_CLIP = "playClip"
    }
}