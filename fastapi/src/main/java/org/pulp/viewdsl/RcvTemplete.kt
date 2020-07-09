package org.pulp.viewdsl

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * RecyclerView ViewHolder
 * Created by xinjun on 2020/7/6 18:50
 */
class VH<T>(v: View) : RecyclerView.ViewHolder(v) {

    var item: View = v
    var mFinder = finder(item) {}
    var itemSegment: Segment<T>? = null

    fun <T : View> get(id: Int): T {
        return mFinder.find(id)
    }
}

/**
 * a common adapter for RecyclerView
 * Created by xinjun on 2020/7/6 16:00
 */
class RecyclerViewAdpt<T>(var segmentSets: SegmentSets) : RecyclerView.Adapter<VH<T>>() {

    constructor(init: () -> SegmentSets) : this(init())

    override fun getItemViewType(position: Int): Int {
        segmentSets.run {
            "pos=$position,isHeader=${isHeader(position)},isFooter=${isFooter(position)},headersize=${headerSize()},datasize=${data.size}".log()
            if (isHeader(position))
                return headerPos2Type(position)
            else if (isFooter(position))
                return footerPos2Type(position)
        }

        var viewType = 0
        segmentSets.typeBlock?.let {
            val realPos = position - segmentSets.headerSize()
            "pos=$position,realpos=$realPos,headersize=${segmentSets.headerSize()}".log()
            val data = TypeInfo(realPos, segmentSets.data.get(realPos))
            viewType = segmentSets.typeBlock!!(data)
        }


        segmentSets.checkViewType(viewType)
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<T> {
        "onCreateViewHolder.viewType=$viewType".log()
        val segment = segmentSets.mSegments[viewType]
        var view: View? = null
        segment?.run {

            val isHeader = segmentSets.isHeader(viewType.toFloat())
            val isFooter = segmentSets.isFooter(viewType.toFloat())

            "onCreateViewHolder.viewType=$viewType,viewInstance=$view,segment=$segment,isHeader=$isHeader,isFooter=$isFooter".log()
            if (viewInstance != null && (isHeader || isFooter)) {
                view = viewInstance
                return@run
            }
            view = LayoutInflater.from(segmentSets.ctx)
                    .inflate(segment.layoutId, parent, false)
            viewInstance = view
        }

        //undefine type,that view can be null
        if (view == null)
            view = View(segmentSets.ctx)


        val vh = VH<T>(view!!)
        @Suppress("UNCHECKED_CAST")
        vh.itemSegment = segment as Segment<T>?
        return vh
    }

    override fun getItemCount() =
            segmentSets.data.size + segmentSets.headerSize() + segmentSets.footerSize()

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: VH<T>, position: Int) {
        holder.itemSegment?.bindCb?.let {
            if (segmentSets.isHeader(position) || segmentSets.isFooter(position))
                BindingContext(holder.mFinder, position, Any() as T).it()
            else {
                val itemData = segmentSets.data[position - segmentSets.headerSize()]
                BindingContext(holder.mFinder, position, itemData as T).it()
            }
        }
    }
}