package org.pulp.viewdsl

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.pulp.viewdsl.anno.ArgIndex
import java.lang.Exception
import java.lang.RuntimeException

/**
 * RecyclerView ViewHolder
 * Created by xinjun on 2020/7/6 18:50
 */
class VH<T>(v: View) : RecyclerView.ViewHolder(v) {

    var item: View = v
    var mFinder = finder(item) {}
    var itemBaseSegment: BaseSegment<T, Any>? = null

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
//            "pos=$position,isHeader=${isHeader(position)},isFooter=${isFooter(position)},headersize=${headerSize()},datasize=${data.size}".log()
            if (isHeader(position))
                return headerPos2Type(position)
            else if (isFooter(position))
                return footerPos2Type(position)
        }

        var viewType = 0
        segmentSets.typeBlock?.let {
            val realPos = position - segmentSets.headerSize()
//            "pos=$position,realpos=$realPos,headersize=${segmentSets.headerSize()}".log()
            val data = TypeInfo(realPos, segmentSets.data.get(realPos))
            viewType = segmentSets.typeBlock!!(data)
        }


        segmentSets.checkViewType(viewType)
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<T> {
        var vh: VH<T>? = null
//        "onCreateViewHolder.viewType=$viewType".log()
        val isHeader = segmentSets.isHeader(viewType.toFloat())
        val isFooter = segmentSets.isFooter(viewType.toFloat())

        val segmentInfo = when {
            isHeader -> segmentSets.mSegmentsHeader[viewType]!!
            isFooter -> segmentSets.mSegmentsFooter[viewType]!!
            else -> segmentSets.mSegments[viewType]!!
        }

        val segment: BaseSegment<*, *>
        try {
            segment = segmentInfo.clazz.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Segment must has no zero argument constructor,please check " +
                    "class" +
                    ":${segmentInfo.clazz.name}")
        }
        segment.ctx = parent.context

        argInject(segmentInfo, segment)


        var view: View? = null
        segment.run {
//            "onCreateViewHolder.viewType=$viewType,viewInstance=$view,segment=$segment,isHeader=$isHeader,isFooter=$isFooter".log()

            if ((isHeader || isFooter) &&
                    segment is SegmentDataNullable &&
                    segment.onCreateViewInstance() != null) {
                view = segment.onCreateViewInstance()
                return@run

            } else {
                view = LayoutInflater.from(segmentSets.ctx)
                        .inflate(onCreateView(), parent, false)

                view.safe {
                    vh = VH(this)
                    vh?.mFinder?.init(this@run, {})
                    onViewCreated(this)
                }
            }
        }
        segmentInfo.view = view

        //undefine type,that view can be null
        if (view == null)
            view = View(segmentSets.ctx)


        if (vh == null)
            vh = VH(view!!)


        @Suppress("UNCHECKED_CAST")
        vh?.itemBaseSegment = segment as BaseSegment<T, Any>?
        return vh!!
    }

    override fun getItemCount() =
            segmentSets.data.size + segmentSets.headerSize() + segmentSets.footerSize()

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: VH<T>, position: Int) {

        holder.itemBaseSegment?.let {
            val itemData: Any?
            val dataNullable: Boolean
            when {
                segmentSets.isHeader(position) -> {
                    dataNullable = true
                    val viewType = segmentSets.headerPos2Type(position)
                    itemData = segmentSets.dataHeader[viewType]
                }
                segmentSets.isFooter(position) -> {
                    dataNullable = true
                    val viewType = segmentSets.footerPos2Type(position)
                    itemData = segmentSets.dataFooter[viewType]
                }
                else -> {
                    dataNullable = false
                    itemData = segmentSets.data[position - segmentSets.headerSize()]
                }
            }

            holder.mFinder.init(it, {})
            if (!dataNullable) {
                val bindingContext = BindingContext(segmentSets.data.size, position, itemData as T)
                if (it is Segment) {
                    it.bindCtx = bindingContext
                    it.onBind(bindingContext)
                }

            } else {
                val bindingContextDataNullable = BindingContextDataNullable(position, itemData as T)
                if (it is SegmentDataNullable) {
                    it.bindCtx = bindingContextDataNullable
                    it.onBind(bindingContextDataNullable)
                }
            }
        }
    }
}
