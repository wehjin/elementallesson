package com.rubyhuntersky.quizmaker.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.interaction.core.Projector.Component as ProjectComponent


abstract class ProjectorActivity<V : Any, A : Any>(
    private val projector: Projector<V, A>
) : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityRegistry.activeActivity = this
        projector.activity = this
    }

    override fun onStart() {
        super.onStart()
        projector.start()
    }

    override fun onStop() {
        projector.stop()
        super.onStop()
    }
}

