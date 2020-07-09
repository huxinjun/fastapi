package org.pulp.viewdsl

import android.content.Context
import android.widget.AdapterView

/**
 * segment info for adapter view
 * Created by xinjun on 2020/7/7 10:27
 */
class LvSegmentSets(var ctx: Context) {


    var data: MutableList<Any> = mutableListOf()
    var mSegment: Segment<*>? = null

    fun <T> item(func: () -> Segment<T>) {
        mSegment = func()
    }
}

inline fun AdapterView<*>.templete(crossinline init: LvSegmentSets.() -> Unit) {
    this.adapter = ItemViewAdapter<Any> {
        val set = LvSegmentSets(context)
        set.init()
        set
    }

}

inline fun AdapterView<*>.data(init: () -> List<Any>) {
    data(false, init)
}

inline fun AdapterView<*>.data(append: Boolean, init: () -> List<Any>) {
    if (adapter == null)
        throw RuntimeException("data invoke must after templete{...}")
    @Suppress("UNCHECKED_CAST")
    with(adapter as ItemViewAdapter<*>) {
        if (!append) set.data.clear()
        set.data.addAll(init())
        notifyDataSetChanged()
    }
}