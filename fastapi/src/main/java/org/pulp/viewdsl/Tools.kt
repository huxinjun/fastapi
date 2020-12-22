package org.pulp.viewdsl

import android.util.Log
import android.view.View


inline fun <T : View> T?.safe(function: T.() -> Unit) {
    if (this == null)
        return
    this.function()
}

fun Int.abs(): Int = Math.abs(this)


fun log(text: String) {
    Log.i("viewdsl", text)
}

fun Any.log() {
    Log.i("viewdsl", toString())
}