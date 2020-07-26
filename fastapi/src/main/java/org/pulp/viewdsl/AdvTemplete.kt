package org.pulp.viewdsl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


class ItemViewAdapter<T>(var set: LvSegmentSets) : BaseAdapter() {

    constructor(init: () -> LvSegmentSets) : this(init())

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val realPos = if (set.mSegment?.repeatable == true) position % set.data.size else position

        val holder: VH<T>


        var view = convertView

        if (view == null || view.tag == null) {
            view =
                    LayoutInflater.from(set.ctx).inflate(set.mSegment?.layoutId ?: 0, parent, false)
            holder = VH(view!!)
            view.tag = holder
        } else {
            "use holder $realPos".log()
            holder = view.tag as VH<T>
        }

        val segment = set.mSegment as Segment<T>

        segment.bindCb?.let {
            val itemData = set.data[realPos]
            BindingContext(holder.mFinder, set.data.size, realPos, itemData as T).it()
        }

        return view

    }

    override fun getItem(position: Int): Any? {
        val repeatable = set.mSegment?.repeatable
        if (repeatable != null && repeatable)
            return set.data[position % set.data.size]
        return set.data[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int {
        val repeatable = set.mSegment?.repeatable
        if (repeatable != null && repeatable)
            return Int.MAX_VALUE
        return set.data.size
    }
}