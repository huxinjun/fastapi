package org.pulp.viewdsl

import android.view.View

/**
 * view可重复使用的片段
 * Created by xinjun on 2020/7/6 15:45
 */
open class Segment<T> {
    var layoutId: Int = 0
    var viewInstance: View? = null//记录列表视图中的header或footer,在item中无效
    var repeatable: Boolean = false//使列表循环显示

    //绑定
    var bindCb: (BindingContext<T>.() -> Unit)? = null

    fun bind(bind: BindingContext<T>.() -> Unit) {
        this.bindCb = bind
    }

    fun repeat() {
        repeatable = true
    }

    fun layout(res: Int) {
        layoutId = res
    }

    fun layout(v: View) {
        viewInstance = v
    }
}

fun <T> segment(func: Segment<T>.() -> Unit): Segment<T> {
    val segment = Segment<T>()
    segment.func()
    return segment
}

/**
 * 数据视图绑定上下文
 * Created by xinjun on 2020/7/6 15:48
 */
class BindingContext<T>(val finder: Finder, var pos: Int, var data: T)