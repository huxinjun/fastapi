package org.pulp.viewdsl

import android.util.Log
import android.view.View


inline fun <T : View> T?.safe(function: T.() -> Unit) {
    if (this == null)
        return
    this.function()
}

fun Int.abs(): Int = Math.abs(this)

fun Int.R(rClass: Any): String {
    rClass::class.java.declaredFields.forEach {
        (it::setAccessible)(true)
        val rId = it.getInt(null)
        if (this == rId)
            return "R.id." + it.name
    }
    return toString()
}

fun log(text: String) {
    Log.i("viewdsl", text)
}

fun Any.log() {
    Log.i("viewdsl", toString())
}