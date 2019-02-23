package com.rubyhuntersky.quizmaker

import android.content.Intent
import com.rubyhuntersky.interaction.core.Portal
import com.rubyhuntersky.interaction.quiz.Action
import com.rubyhuntersky.interaction.quiz.QuizInteraction
import com.rubyhuntersky.quizmaker.android.ActivityRegistry

object QuizPortal : Portal<String> {

    val interaction = QuizInteraction(LearnerBook)

    override fun jump(carry: String) {
        interaction.sendAction(Action.Load(carry))
        ActivityRegistry.activeActivity.startActivity(
            Intent(ActivityRegistry.activeActivity, QuizActivity::class.java)
        )
    }
}