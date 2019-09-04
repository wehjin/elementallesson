package com.rubyhuntersky.quizmaker

import androidx.leanback.widget.GuidanceStylist
import com.rubyhuntersky.data.Lesson
import com.rubyhuntersky.data.material.core.LessonType
import com.rubyhuntersky.quizmaker.android.toRelativeString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class AnswerFragment : StepFragment() {

    suspend fun setSight(lesson: Lesson, events: Channel<String>) {
        control.send(
            Msg.SetView(
                guidance = getGuidance(lesson),
                buttons = listOf(
                    Button("Back", event = CANCEL_ANSWER),
                    Button(
                        "Repeat",
                        event = ANSWER_HARD,
                        subtext = "Hard. Repeat soon."
                    ),
                    Button(
                        "Space",
                        event = ANSWER_EASY,
                        subtext = "Easy. Repeat in ${lesson.restDurationWithEasy(LocalDateTime.now()).toRelativeString()}."
                    )
                ),
                events = events
            )
        )
    }

    private fun getGuidance(lesson: Lesson): GuidanceStylist.Guidance {
        return when (lesson.type) {
            LessonType.LISTENING -> {
                val title = lesson.response
                val description = lesson.responseColor ?: ""
                GuidanceStylist.Guidance(title, description, "", null)
            }
            else -> {
                val title = lesson.response
                val description = lesson.responseColor ?: ""
                val breadcrumb = lesson.prompt
                GuidanceStylist.Guidance(title, description, breadcrumb, null)
            }
        }
    }

    companion object {
        const val ANSWER_EASY = "recordEasy"
        const val ANSWER_HARD = "recordHard"
        const val CANCEL_ANSWER = "backToLesson"
    }
}