package org.pulp.viewdsl

import android.view.View


/**
 * view可重复使用的片段
 * Created by xinjun on 2020/7/6 15:45
 */
abstract class BaseSegment<T, B> {

    var layoutId: Int = 0
    var viewInstance: View? = null//记录列表视图中的header或footer,在item中无效
    var repeatable: Boolean = false//使列表循环显示

    //绑定
    var bindCb: (B.() -> Unit)? = null

    fun bind(bind: B.() -> Unit) {
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

open class Segment<T> : BaseSegment<T, BindingContext<T>>()

/**
 * 因为header,footer的view不是根据数据来实例化的
 * 会出现没有数据但是需要显示视图的情况
 * 为了解决此类情况,衍生出此类用于解决header数据问题
 * Created by xinjun on 2020/7/23 12:20 AM
 */
open class SegmentDataNullable<T> : BaseSegment<T, BindingContextDataNullable<T>>()


/**
 * 数据视图绑定上下文
 * Created by xinjun on 2020/7/6 15:48
 */
class BindingContext<T>(val finder: Finder, var pos: Int, var data: T)

/**
 * 数据视图绑定上下文,数据可为null
 * Created by xinjun on 2020/7/23 12:22 AM
 */
class BindingContextDataNullable<T>(val finder: Finder, var pos: Int, var data: T?)


fun <T> segment(func: Segment<T>.() -> Unit): Segment<T> {
    val segment = Segment<T>()
    segment.func()
    return segment
}

fun <T> segmentDataNullable(func: SegmentDataNullable<T>.() -> Unit): SegmentDataNullable<T> {
    val segment = SegmentDataNullable<T>()
    segment.func()
    return segment
}