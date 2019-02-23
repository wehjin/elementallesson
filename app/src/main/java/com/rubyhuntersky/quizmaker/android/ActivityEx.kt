package com.rubyhuntersky.quizmaker.android

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

fun <V : View> AppCompatActivity.findViewByIdInLayout(@IdRes viewId: Int, @LayoutRes layoutId: Int, init: (V) -> Unit): V {
    return this.findViewById(viewId) ?: run {
        setContentView(layoutId)
        findViewById<V>(viewId).apply(init)
    }
}