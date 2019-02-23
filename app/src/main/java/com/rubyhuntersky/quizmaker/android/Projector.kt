package com.rubyhuntersky.quizmaker.android

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.interaction.core.Interaction
import io.reactivex.android.schedulers.AndroidSchedulers
import com.rubyhuntersky.interaction.core.Projector as CoreProjector

class Projector<V : Any, A : Any>(
    private val activityName: String,
    interaction: Interaction<V, A>
) {
    lateinit var activity: AppCompatActivity

    private var projector = CoreProjector(
        interaction = interaction,
        observeOn = AndroidSchedulers.mainThread(),
        log = { Log.d(activityName, "VISION: $it") }
    )

    fun start() = projector.start()
    fun stop() = projector.stop()

    interface Component<V : Any, V2 : V, A : Any> {
        fun convert(vision: V, activity: AppCompatActivity): V2?
        fun render(vision: V2, sendAction: (A) -> Unit, activity: AppCompatActivity)
    }

    fun <V2 : V> addComponent(component: Component<V, V2, A>): Projector<V, A> {
        projector = projector.addComponent(object : CoreProjector.Component<V, V2, A> {
            @Suppress("UNCHECKED_CAST")
            override fun convert(vision: V): V2? = component.convert(vision, activity)

            override fun render(vision: V2, sendAction: (A) -> Unit) = component.render(
                vision,
                { action: A ->
                    Log.d(activityName, "ACTION: $action")
                    sendAction(action)
                },
                activity
            )
        })
        return this
    }
}
