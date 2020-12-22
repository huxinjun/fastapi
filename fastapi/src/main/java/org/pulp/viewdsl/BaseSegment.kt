package org.pulp.viewdsl

import android.view.View


/**
 * view可重复使用的片段
 * Created by xinjun on 2020/7/6 15:45
 */
abstract class BaseSegment<T, B> {

    var name: String? = null

    //第二种使用方式,子类可重写生命周期方法-------------------------
    open fun onBind(bindCtx: B) {}
    open fun onCreateView(): Int = 0
    open fun onViewCreated(view: View) {}
    open fun onReceiveArg(args: Array<out Any>) {}


}

open class Segment<T> : BaseSegment<T, BindingContext<T>>() {

    internal var bindCtx: BindingContext<T>? = null

    fun getData(block: BindingContext<T>.() -> Unit) {
        bindCtx?.let {
            block(it)
        }
    }
}

/**
 * 因为header,footer的view不是根据数据来实例化的
 * 会出现没有数据但是需要显示视图的情况
 * 为了解决此类情况,衍生出此类用于解决header数据问题
 * Created by xinjun on 2020/7/23 12:20 AM
 */
open class SegmentDataNullable<T> : BaseSegment<T, BindingContextDataNullable<T>>() {

    internal var bindCtx: BindingContextDataNullable<T>? = null

    open fun onCreateViewInstance(): View? = null

    fun getData(block: BindingContextDataNullable<T>.() -> Unit) {
        bindCtx?.let {
            block(it)
        }
    }
}


/**
 * 数据视图绑定上下文
 * Created by xinjun on 2020/7/6 15:48
 */
class BindingContext<T>(var size: Int, var pos: Int, var data: T)

/**
 * 数据视图绑定上下文,数据可为null
 * Created by xinjun on 2020/7/23 12:22 AM
 */
class BindingContextDataNullable<T>(var pos: Int, var data: T?)