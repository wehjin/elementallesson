package com.rubyhuntersky.quizmaker.app

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi

interface AppScope {
    fun getApplication(): Application
}

@ExperimentalCoroutinesApi
val AppScope.app: App
    get() = getApplication() as App

@Suppress("unused")
val AppScope.TAG: String
    get() = this::class.java.simpleName
