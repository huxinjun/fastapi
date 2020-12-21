package org.pulp.viewdsl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.lang.Exception
import java.lang.RuntimeException


class ItemViewAdapter<T>(var set: LvSegmentSets) : BaseAdapter() {

    constructor(init: () -> LvSegmentSets) : this(init())

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val holder: VH<T>
        var view = convertView


        val segmentInfo = set.mSegmentInfo ?: return View(set.ctx)
        val segment: BaseSegment<T, BindingContext<T>>

        if (view == null || view.tag == null) {
            //创建segment
            try {
                segment = segmentInfo.clazz.newInstance() as BaseSegment<T, BindingContext<T>>
            } catch (e: Exception) {
                throw RuntimeException("Segment must has no zero argument constructor,please check " +
                        "class" +
                        ":${segmentInfo.clazz.name}")
            }
            segmentInfo.args?.let {
                segment.onReceiveArg(it)
            }

            view = LayoutInflater.from(set.ctx).inflate(segment.onCreateView(),
                    parent, false)
            holder = VH(view!!)
            holder.itemBaseSegment = segment as BaseSegment<T, Any>
            view.tag = holder
        } else {
            holder = view.tag as VH<T>
            segment = holder.itemBaseSegment as BaseSegment<T, BindingContext<T>>
        }

        //刷新视图
        holder.mFinder.init(segment, {})
        val realPos = if (segmentInfo.repeatable) position % set.data.size else position
        val itemData = set.data[realPos]
        val bindingContext = BindingContext(set.data.size, realPos, itemData as T)
        segment.onBind(bindingContext)

        return view

    }

    override fun getItem(position: Int): Any? {
        val repeatable = set.mSegmentInfo?.repeatable
        if (repeatable != null && repeatable)
            return set.data[position % set.data.size]
        return set.data[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int {
        val repeatable = set.mSegmentInfo?.repeatable
        if (repeatable != null && repeatable)
            return Int.MAX_VALUE
        return set.data.size
    }
}