package com.rubyhuntersky.quizmaker

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.rubyhuntersky.quizmaker.app.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class DegreeActivity : FragmentActivity(), CoroutineScope, AppScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_degree)
    }
}
