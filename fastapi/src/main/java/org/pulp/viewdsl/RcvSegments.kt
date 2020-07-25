package org.pulp.viewdsl

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlin.math.pow

/**
 * type info,include pos and data
 * Created by xinjun on 2020/7/7 10:21
 */
class TypeInfo(p: Int, d: Any?) {
    var pos: Int = p
        private set
        get
    var data: Any? = d
        private set
        get
}

/**
 * record info about RecyclerView adapter
 * Created by xinjun on 2020/7/8 11:12
 */
class SegmentSets(var ctx: Context) {


    private val headerInitIndex = -1//must litter than 0
    private val headerCapacity = 100
    private val footerInitIndex = headerInitIndex - headerCapacity
    private val footerCapacity = 100
    private var headerTypeIndex = headerInitIndex
    private var footerTypeIndex = footerInitIndex


    var data: MutableList<Any> = mutableListOf()
    var dataHeader: MutableMap<Int, Any> = mutableMapOf()
    var dataFooter: MutableMap<Int, Any> = mutableMapOf()

    var typeBlock: (TypeInfo.() -> Int)? = null
    var spanBlock: (Int.() -> Int)? = null
    var mSegments = mutableMapOf<Int, BaseSegment<*, *>>()


    fun type(block: TypeInfo.() -> Int) {
        this.typeBlock = block
    }

    /**
     * span set
     * it is viewtype
     */
    fun span(block: Int.() -> Int) {
        this.spanBlock = block
    }


    fun <T> header(func: () -> SegmentDataNullable<T>) {
        if (headerTypeIndex <= footerInitIndex)
            throw RuntimeException("header max support count $headerCapacity")
        mSegments.put(headerTypeIndex--, func())
    }

    fun <T> footer(func: () -> SegmentDataNullable<T>) {
        if (footerTypeIndex <= footerInitIndex - footerCapacity)
            throw RuntimeException("footer max support count $footerCapacity")
        @Suppress("UNCHECKED_CAST")
        mSegments.put(footerTypeIndex--, func())
    }

    fun <T> item(type: Int, func: () -> Segment<T>) {
        checkViewType(type)
        mSegments.put(type, func())
    }

    fun <T> item(func: () -> Segment<T>) {
        item(0, func)
    }

    fun checkViewType(viewType: Int) {
        if (viewType <= headerInitIndex)
            throw RuntimeException(
                    "item view type must be equal or greatter than $headerInitIndex" +
                            ",because header and footer was used view type begin $headerInitIndex to ${footerInitIndex - footerCapacity + 1}"
            )
    }

    fun headerSize(): Int {
        var count = 0
        mSegments.keys.forEach {
            if (it <= headerInitIndex && it > footerInitIndex)
                count++
        }
        return count
    }

    fun footerSize(): Int {
        var count = 0
        mSegments.keys.forEach {
            if (it <= footerInitIndex && it > footerInitIndex - footerCapacity)
                count++
        }
        return count
    }

    fun isHeader(viewType: Float): Boolean =
            viewType.toInt() in (footerInitIndex + 1)..headerInitIndex

    fun isHeader(position: Int): Boolean = position < headerSize()

    fun isFooter(viewType: Float): Boolean =
            viewType.toInt() <= footerInitIndex && viewType.toInt() > footerInitIndex - footerCapacity

    fun isFooter(position: Int): Boolean = position >= headerSize() + data.size

    fun headerPos2Type(pos: Int): Int = headerInitIndex - pos

    fun headerType2Pos(viewType: Int): Int = viewType.abs() - headerInitIndex.abs()

    fun footerPos2Type(pos: Int): Int = footerInitIndex - (pos - headerSize() - data.size)

    fun footerType2Pos(viewType: Int): Int =
            viewType.abs() - footerInitIndex.abs() + (headerSize() + data.size)

    fun headerIndex2ViewType(i: Int) = headerInitIndex - i

    fun footerIndex2ViewType(i: Int) = footerInitIndex - i
}


@Suppress("UNCHECKED_CAST")
inline fun RecyclerView.templete(crossinline init: SegmentSets.() -> Unit) {
    this.adapter = RecyclerViewAdpt<Any> {
        val set = SegmentSets(context)
        set.init()
        val data = getTag(2.toDouble().pow(30.toDouble()).toInt())
        val dataHeader = getTag(2.toDouble().pow(30.toDouble()).toInt() + 1)
        val dataFooter = getTag(2.toDouble().pow(30.toDouble()).toInt() + 2)
        if (data is MutableList<*>)
            set.data = data as MutableList<Any>
        if (dataHeader is MutableMap<*, *>)
            set.dataHeader = dataHeader as MutableMap<Int, Any>
        if (dataFooter is MutableMap<*, *>)
            set.dataFooter = dataFooter as MutableMap<Int, Any>
        post {
            layoutManager?.run {
                if (this is GridLayoutManager) {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(pos: Int): Int {
                            var spansize = 1
                            if (set.isHeader(pos) || set.isFooter(pos))
                                spansize = this@run.spanCount
                            else {
                                set.spanBlock?.run {
                                    val realPos = pos - set.headerSize()
                                    val data = TypeInfo(realPos, set.data.get(realPos))
                                    set.typeBlock?.let {
                                        val viewType = it(data)

                                        spansize = this(viewType)
                                    }
                                }
                            }
                            return spansize
                        }
                    }
                }
            }

        }
        set
    }
}


inline fun RecyclerView.data(init: Config.() ->
List<Any>) {
    val config = Config(false, false, -1, true)
    if (config.appendToHead)
        config.appendPos = 0
    if (config.appendPos >= 0)
        config.append = true
    val datas = config.init()
    if (adapter == null) {
        setTag(2.toDouble().pow(30.toDouble()).toInt(), datas)
        return
    }
    @Suppress("UNCHECKED_CAST")
    val adpt = adapter as RecyclerViewAdpt<*>
    with(adpt) {
        if (!config.append) segmentSets.data.clear()
        var insertPos = config.appendPos
        if (config.appendPos < 0)
            insertPos = segmentSets.data.size
        segmentSets.data.addAll(insertPos, datas)
        if (config.notify)
            notifyDataSetChanged()
    }

}

/**
 * set data config
 * Created by xinjun on 2020/7/25 11:46 AM
 */
data class Config(var append: Boolean, var appendToHead: Boolean, var appendPos: Int, var notify:
Boolean) {
    /**
     *清除数据
     */
    fun clear(): List<Any> {
        append = false
        appendToHead = false
        appendPos = -1
        return emptyList()
    }
}


fun RecyclerView.dataHeader(pos: Int, data: Any) {
    if (adapter == null) {
        val tag = getTag(2.toDouble().pow(30.toDouble()).toInt() + 1)
        if (tag == null) {
            val mutableMapOf = mutableMapOf<Int, Any>()
            mutableMapOf.put(pos, data)
            setTag(2.toDouble().pow(30.toDouble()).toInt() + 1, mutableMapOf)
        } else {
            @Suppress("UNCHECKED_CAST")
            val map = tag as MutableMap<Int, Any>
            map.put(pos, data)
        }
        return
    }
    @Suppress("UNCHECKED_CAST")
    val adpt = adapter as RecyclerViewAdpt<*>
    with(adpt) {
        segmentSets.dataHeader.put(pos, data)
        notifyDataSetChanged()
    }

}

fun RecyclerView.dataFooter(pos: Int, data: Any) {
    if (adapter == null) {
        val tag = getTag(2.toDouble().pow(30.toDouble()).toInt() + 2)
        if (tag == null) {
            val mutableMapOf = mutableMapOf<Int, Any>()
            mutableMapOf.put(pos, data)
            setTag(2.toDouble().pow(30.toDouble()).toInt() + 2, mutableMapOf)
        } else {
            @Suppress("UNCHECKED_CAST")
            val map = tag as MutableMap<Int, Any>
            map.put(pos, data)
        }
        return
    }
    @Suppress("UNCHECKED_CAST")
    val adpt = adapter as RecyclerViewAdpt<*>
    with(adpt) {
        segmentSets.dataFooter.put(pos, data)
        notifyDataSetChanged()
    }

}

fun RecyclerView.dissmissHeader(i: Int) {
    val header = header(i)
    header?.run {
        tag = layoutParams.height
        layoutParams.height = 0
        requestLayout()
    }
}

fun RecyclerView.showHeader(i: Int) {
    val header = header(i)
    header?.run {
        if (tag is Int) {
            layoutParams.height = tag as Int
            requestLayout()
        }
    }
}

fun RecyclerView.dissmissFooter(i: Int) {
    val footer = footer(i)
    footer?.run {
        tag = layoutParams.height
        layoutParams.height = 0
        requestLayout()
    }
}

fun RecyclerView.showFooter(i: Int) {
    val footer = footer(i)
    footer?.run {
        if (tag is Int) {
            layoutParams.height = tag as Int
            requestLayout()
        }
    }
}

//**************************************
/**
 * get a header view by position from RecyclerView
 */
fun RecyclerView.header(i: Int): View? {
    if (adapter == null)
        return null
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    val targetViewType = segmentSets.headerIndex2ViewType(i)
    segmentSets.mSegments.forEach { (viewType, segment) ->
        segmentSets.run {
            "header.targetViewType=$targetViewType,curr=$viewType".log()
            if (targetViewType == viewType)
                return segment.viewInstance
        }
    }
    return null
}

/**
 * get a footer view by position from RecyclerView
 */
fun RecyclerView.footer(i: Int): View? {
    if (adapter == null)
        return null
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    val targetViewType = segmentSets.footerIndex2ViewType(i)
    segmentSets.mSegments.forEach { (viewType, segment) ->
        segmentSets.run {
            if (targetViewType == viewType)
                return segment.viewInstance
        }
    }
    return null
}