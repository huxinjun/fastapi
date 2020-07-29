package org.pulp.viewdsl

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import java.lang.Exception
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

    data class SegmentScope(var name: String?)

    companion object {
        val headerInitIndex = -1//must litter than 0
        val headerCapacity = 100
        val footerInitIndex = headerInitIndex - headerCapacity
        val footerCapacity = 100
    }

    var headerTypeIndex = headerInitIndex
    var footerTypeIndex = footerInitIndex

    var headerSize = -1
    var footerSize = -1


    //item使用的数据,key:position
    var data: MutableList<Any> = mutableListOf()

    //header使用的数据,key:viewtype
    var dataHeader: MutableMap<Int, Any> = mutableMapOf()

    //footer使用的数据,key:viewtype
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


    fun <T> header(func: SegmentScope.() -> SegmentDataNullable<T>) {
        if (headerTypeIndex <= footerInitIndex)
            throw RuntimeException("header max support count $headerCapacity")
        val scope = SegmentScope(null)
        val segment = scope.func()
        segment.name = scope.name
        mSegments[headerTypeIndex--] = segment
    }


    fun <T> footer(func: SegmentScope.() -> SegmentDataNullable<T>) {
        if (footerTypeIndex <= footerInitIndex - footerCapacity)
            throw RuntimeException("footer max support count $footerCapacity")
        val scope = SegmentScope(null)
        val segment = scope.func()
        segment.name = scope.name
        mSegments[footerTypeIndex--] = segment
    }

    fun <T> item(type: Int, func: () -> Segment<T>) {
        checkViewType(type)
        mSegments[type] = func()
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
        if (headerSize != -1)
            return headerSize
        headerSize = 0
        mSegments.keys.forEach {
            if (it <= headerInitIndex && it > footerInitIndex)
                headerSize++
        }
        return headerSize
    }

    fun footerSize(): Int {
        if (footerSize != -1)
            return footerSize
        footerSize = 0
        mSegments.keys.forEach {
            if (it <= footerInitIndex && it > footerInitIndex - footerCapacity)
                footerSize++
        }
        return footerSize
    }

    fun isHeader(viewType: Float): Boolean =
            viewType.toInt() in (footerInitIndex + 1)..headerInitIndex

    fun isFooter(viewType: Float): Boolean =
            viewType.toInt() <= footerInitIndex && viewType.toInt() > footerInitIndex - footerCapacity

    fun isHeader(position: Int): Boolean = position < headerSize()
    fun isFooter(position: Int): Boolean = position >= headerSize() + data.size

    fun headerPos2Type(pos: Int): Int {
        val headerTypeList = mutableListOf<Int>()
        mSegments.keys.forEach {
            if (isHeader(it.toFloat()))
                headerTypeList.add(it)
        }
        return headerTypeList[pos]
    }

    fun footerPos2Type(pos: Int): Int {
        val footerTypeList = mutableListOf<Int>()
        mSegments.keys.forEach {
            if (isFooter(it.toFloat()))
                footerTypeList.add(it)
        }
        val newPos = pos - headerSize() - data.size
        return footerTypeList[newPos]
    }

    //header索引转为viewtype
    fun headerIndex2ViewType(i: Int) = headerPos2Type(i)

    //footer索引转为viewtype
    fun footerIndex2ViewType(i: Int): Int {
        val footerTypeList = mutableListOf<Int>()
        mSegments.keys.forEach {
            if (isFooter(it.toFloat()))
                footerTypeList.add(it)
        }
        return footerTypeList[i]
    }
}
//**************************************

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
//**************************************

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
 * get RecyclerView data
 */
fun RecyclerView.getData(cb: MutableList<Any>.() -> Unit) {
    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    try {
        @Suppress("UNCHECKED_CAST")
        segmentSets.data.cb()
    } catch (ex: Exception) {
        "RecyclerView.getData occur a exception:$ex".log()
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


//**************************************


fun RecyclerView.dataHeader(pos: Int, data: Any) {
    var viewType: Int
    if (adapter == null) {
        viewType = SegmentSets.headerInitIndex - pos
        val tag = getTag(2.toDouble().pow(30.toDouble()).toInt() + 1)
        if (tag == null) {
            val mutableMapOf = mutableMapOf<Int, Any>()
            mutableMapOf[viewType] = data
            setTag(2.toDouble().pow(30.toDouble()).toInt() + 1, mutableMapOf)
        } else {
            @Suppress("UNCHECKED_CAST")
            val map = tag as MutableMap<Int, Any>
            map[viewType] = data
        }
        return
    }
    @Suppress("UNCHECKED_CAST")
    val adpt = adapter as RecyclerViewAdpt<*>
    with(adpt) {
        viewType = segmentSets.headerIndex2ViewType(pos)
        segmentSets.dataHeader[viewType] = data
        notifyDataSetChanged()
    }

}

fun RecyclerView.dataFooter(pos: Int, data: Any) {
    var viewType: Int
    if (adapter == null) {
        viewType = SegmentSets.footerInitIndex - pos
        val tag = getTag(2.toDouble().pow(30.toDouble()).toInt() + 2)
        if (tag == null) {
            val mutableMapOf = mutableMapOf<Int, Any>()
            mutableMapOf[viewType] = data
            setTag(2.toDouble().pow(30.toDouble()).toInt() + 2, mutableMapOf)
        } else {
            @Suppress("UNCHECKED_CAST")
            val map = tag as MutableMap<Int, Any>
            map[viewType] = data
        }
        return
    }
    @Suppress("UNCHECKED_CAST")
    val adpt = adapter as RecyclerViewAdpt<*>
    with(adpt) {
        viewType = segmentSets.footerIndex2ViewType(pos)
        segmentSets.dataFooter[viewType] = data
        notifyDataSetChanged()
    }

}

//**************************************
/**
 * insert a header segment to RecyclerView
 */
inline fun <T> RecyclerView.headerAdd(pos: Int, func: SegmentSets.SegmentScope.() ->
SegmentDataNullable<T>) {
    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        if (headerTypeIndex <= SegmentSets.footerInitIndex)
            throw RuntimeException("header max support count ${SegmentSets.headerCapacity}")

        val viewTypeList: List<Int> = mSegments.keys.toList()


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= headerSize())
            newPos = headerSize()

        //before insert data
        val newSegments = mutableMapOf<Int, BaseSegment<*, *>>()
        var i = 0
        while (i < newPos) {
            val type: Int = viewTypeList[i]
            newSegments[type] = mSegments[type] as BaseSegment<*, *>
            i++
        }

        //insert data
        val segScope = SegmentSets.SegmentScope(null)
        val newSeg = segScope.func()
        newSeg.name = segScope.name
        newSegments[headerTypeIndex--] = newSeg

        //after insert data
        i = newPos
        while (i < viewTypeList.size) {
            val type = viewTypeList[i]
            newSegments[type] = mSegments[type] as BaseSegment<*, *>
            i++
        }

        headerSize = -1//需要重新计算
        mSegments = newSegments
        adapter?.notifyDataSetChanged()
    }

}


/**
 * remove a header segment to RecyclerView
 */
fun RecyclerView.headerRemove(pos: Int) {

    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets

    segmentSets.run {
        val viewTypeList = mutableListOf<Int>()

        mSegments.keys.forEach {
            if (isHeader(it.toFloat()))
                viewTypeList.add(it)
        }

        if (viewTypeList.size == 0)
            return


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= viewTypeList.size)
            newPos = viewTypeList.size - 1

        val type: Int = viewTypeList[newPos]
        mSegments.remove(type)
        headerSize = -1
        adapter?.notifyDataSetChanged()
    }

}

/**
 * remove a header segment to RecyclerView by name
 */
fun RecyclerView.headerRemove(name: String) {
    val index = index(name)
    if (index >= 0)
        headerRemove(index)
}


/**
 * insert a footer segment to RecyclerView
 */
inline fun <T> RecyclerView.footerAdd(pos: Int, func: SegmentSets.SegmentScope.() ->
SegmentDataNullable<T>) {
    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        if (footerTypeIndex <= SegmentSets.footerInitIndex - SegmentSets.footerCapacity)
            throw RuntimeException("footer max support count ${SegmentSets.footerCapacity}")

        val viewTypeList = mSegments.keys.toList()


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= footerSize())
            newPos = footerSize()

        //before insert data
        val newSegments = mutableMapOf<Int, BaseSegment<*, *>>()
        var i = 0
        while (i < headerSize() + data.size + newPos) {
            val type: Int = viewTypeList[i]
            newSegments[type] = mSegments[type] as BaseSegment<*, *>
            i++
        }

        //insert data
        val segScope = SegmentSets.SegmentScope(null)
        val newSeg = segScope.func()
        newSeg.name = segScope.name
        newSegments[footerTypeIndex--] = newSeg

        //after insert data
        i = headerSize() + data.size + newPos
        while (i < viewTypeList.size) {
            val type = viewTypeList[i]
            newSegments[type] = mSegments[type] as BaseSegment<*, *>
            i++
        }

        footerSize = -1//需要重新计算
        mSegments = newSegments
        adapter?.notifyDataSetChanged()
    }

}


/**
 * remove a footer segment to RecyclerView
 */
fun RecyclerView.footerRemove(pos: Int) {

    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets

    segmentSets.run {
        val viewTypeList = mutableListOf<Int>()

        mSegments.keys.forEach {
            if (isFooter(it.toFloat()))
                viewTypeList.add(it)
        }

        if (viewTypeList.size == 0)
            return


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= viewTypeList.size)
            newPos = viewTypeList.size - 1

        val type: Int = viewTypeList[newPos]
        mSegments.remove(type)
        footerSize = -1
        adapter?.notifyDataSetChanged()
    }

}


/**
 * remove a footer segment to RecyclerView
 */
fun RecyclerView.footerRemove(name: String) {
    val index = index(name)
    if (index >= 0)
        footerRemove(index)
}


/**
 * get header or footer index by name,not fount return -1
 * if has same name,return first
 */
fun RecyclerView.index(name: String): Int {
    if (adapter == null)
        return -1
    if (name.isEmpty())
        return -1
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets

    segmentSets.run {
        val viewTypeList = mutableListOf<Int>()

        //找header
        mSegments.keys.forEach {
            if (isHeader(it.toFloat()))
                viewTypeList.add(it)
        }
        var i = 0
        viewTypeList.forEach {
            if (name == mSegments[it]?.name)
                return i
            i++
        }
        //找footer
        viewTypeList.clear()
        mSegments.keys.forEach {
            if (isFooter(it.toFloat()))
                viewTypeList.add(it)
        }
        i = 0
        viewTypeList.forEach {
            if (name == mSegments[it]?.name)
                return i
            i++
        }
    }

    return -1
}

fun RecyclerView.headerSize(): Int {
    if (adapter == null)
        return 0
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    return segmentSets.headerSize()
}

fun RecyclerView.footerSize(): Int {
    if (adapter == null)
        return 0
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    return segmentSets.footerSize()
}

//**************************************
/**
 * get a header view by name from RecyclerView
 */
fun RecyclerView.header(name: String): View? {
    if (adapter == null)
        return null
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets

    segmentSets.run {
        val viewTypeList = mutableListOf<Int>()

        //找header
        mSegments.keys.forEach {
            if (isHeader(it.toFloat()))
                viewTypeList.add(it)
        }
        var i = 0
        viewTypeList.forEach {
            if (name == mSegments[it]?.name)
                return mSegments[it]?.viewInstance
            i++
        }
    }
    return null
}

/**
 * get a header view by name from RecyclerView
 */
fun RecyclerView.footer(name: String): View? {
    if (adapter == null)
        return null
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        val viewTypeList = mutableListOf<Int>()

        //找header
        mSegments.keys.forEach {
            if (isFooter(it.toFloat()))
                viewTypeList.add(it)
        }
        var i = 0
        viewTypeList.forEach {
            if (name == mSegments[it]?.name)
                return mSegments[it]?.viewInstance
            i++
        }
    }
    return null
}

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