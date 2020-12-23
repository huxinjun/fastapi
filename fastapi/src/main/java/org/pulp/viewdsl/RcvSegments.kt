@file:Suppress("UNCHECKED_CAST")

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

open class SegmentInfo<T>(var clazz: Class<T>) {
    var name: String? = null
    var view: View? = null
    var args: Array<out Any>? = null
}




/**
 * record info about RecyclerView adapter
 * Created by xinjun on 2020/7/8 11:12
 */
class SegmentSets(var ctx: Context) {

    open class SegmentScope {
        var name: String? = null
        var args: Array<out Any>? = null
        fun <T> Class<T>.withName(name: String): Class<T> {
            this@SegmentScope.name = name
            return this
        }

        fun <T> Class<T>.withArgs(vararg args: Any): Class<T> {
            this@SegmentScope.args = args
            return this
        }
    }

    companion object {
        const val headerInitIndex = -1//must litter than 0
        const val headerCapacity = 100
        const val footerInitIndex = headerInitIndex - headerCapacity
        const val footerCapacity = 100
    }

    var headerTypeIndex = headerInitIndex
    var footerTypeIndex = footerInitIndex


    //item使用的数据,key:position
    var data: MutableList<Any> = mutableListOf()

    //header使用的数据,key:viewtype
    var dataHeader: MutableMap<Int, Any> = mutableMapOf()

    //footer使用的数据,key:viewtype
    var dataFooter: MutableMap<Int, Any> = mutableMapOf()

    var typeBlock: (TypeInfo.() -> Int)? = null
    var spanBlock: (Int.() -> Int)? = null
    var mSegments = mutableMapOf<Int, SegmentInfo<out Segment<*>>>()
    var mSegmentsHeader = mutableMapOf<Int, SegmentInfo<out SegmentDataNullable<*>>>()
    var mSegmentsFooter = mutableMapOf<Int, SegmentInfo<out SegmentDataNullable<*>>>()


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


    fun header(func: SegmentScope.() -> Class<out SegmentDataNullable<*>>) {
        if (headerTypeIndex <= footerInitIndex)
            throw RuntimeException("header max support count $headerCapacity")
        val scope = SegmentScope()
        val segmentClass = scope.func()
        val segmentInfo = newSegmentInfoDataNullable(segmentClass)
        if (!scope.name.isNullOrEmpty())
            segmentInfo.name = scope.name
        segmentInfo.args = scope.args
        mSegmentsHeader[headerTypeIndex--] = segmentInfo
    }


    fun footer(func: SegmentScope.() -> Class<out SegmentDataNullable<*>>) {
        if (footerTypeIndex <= footerInitIndex - footerCapacity)
            throw RuntimeException("footer max support count $footerCapacity")
        val scope = SegmentScope()
        val segmentClass = scope.func()
        val segmentInfo = newSegmentInfoDataNullable(segmentClass)
        if (!scope.name.isNullOrEmpty())
            segmentInfo.name = scope.name
        segmentInfo.args = scope.args
        mSegmentsFooter[footerTypeIndex--] = segmentInfo
    }

    fun item(type: Int, func: SegmentScope.() -> Class<out Segment<*>>) {
        checkViewType(type)
        val scope = SegmentScope()
        val segmentClass = scope.func()
        val segmentInfo = newSegmentInfo(segmentClass)
        segmentInfo.args = scope.args
        mSegments[type] = segmentInfo
    }

    fun item(func: SegmentScope.() -> Class<out Segment<*>>) {
        item(0, func)
    }

    private fun newSegmentInfo(clazz: Class<out Segment<*>>) = SegmentInfo(clazz)
    fun newSegmentInfoDataNullable(clazz: Class<out SegmentDataNullable<*>>) = SegmentInfo(clazz)

    //----------------------------------------------------------------------------------------------

    fun checkViewType(viewType: Int) {
        if (viewType <= headerInitIndex)
            throw RuntimeException(
                    "item view type must be equal or greatter than $headerInitIndex" +
                            ",because header and footer was used view type begin " +
                            "$headerInitIndex to ${footerInitIndex - footerCapacity + 1}"
            )
    }

    fun headerSize() = mSegmentsHeader.size

    fun itemSize() = mSegments.size

    fun footerSize() = mSegmentsFooter.size

    fun isHeader(viewType: Float): Boolean =
            viewType.toInt() in (footerInitIndex + 1)..headerInitIndex

    fun isFooter(viewType: Float): Boolean =
            viewType.toInt() <= footerInitIndex && viewType.toInt() > footerInitIndex - footerCapacity

    fun isHeader(position: Int): Boolean = position < headerSize()
    fun isFooter(position: Int): Boolean = position >= headerSize() + data.size

    fun headerPos2Type(pos: Int) = mSegmentsHeader.keys.toList()[pos]

    fun footerPos2Type(pos: Int) = footerIndex2ViewType(pos - headerSize() - data.size)

    //header索引转为viewtype
    fun headerIndex2ViewType(index: Int) = headerPos2Type(index)

    //footer索引转为viewtype
    fun footerIndex2ViewType(index: Int) = mSegmentsFooter.keys.toList()[index]
}
//**************************************

@Suppress("UNCHECKED_CAST")
inline fun RecyclerView.template(crossinline init: SegmentSets.() -> Unit) {
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


fun RecyclerView.dataHeader(index: Int, data: Any) {
    var viewType: Int
    if (adapter == null) {
        viewType = SegmentSets.headerInitIndex - index
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
        if (index < 0) {
            "dataHeader faild,because index little than 0".log()
            return
        }
        if (index >= headerSize()) {
            "dataHeader faild,because index bigger than header size,header size=${headerSize()},index=$index".log()
            return
        }
        viewType = segmentSets.headerIndex2ViewType(index)
        segmentSets.dataHeader[viewType] = data
        notifyDataSetChanged()
    }

}

fun RecyclerView.dataFooter(index: Int, data: Any) {
    var viewType: Int
    if (adapter == null) {
        viewType = SegmentSets.footerInitIndex - index
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
        if (index < 0) {
            "dataFooter faild,because index little than 0".log()
            return
        }
        if (index >= footerSize()) {
            "dataFooter faild,because index bigger than footer size,footer size=${footerSize()}index=$index".log()
            return
        }
        viewType = segmentSets.footerIndex2ViewType(index)
        segmentSets.dataFooter[viewType] = data
        notifyDataSetChanged()
    }

}

//**************************************
/**
 * insert a header segment to RecyclerView
 */
inline fun RecyclerView.headerAdd(pos: Int, func: SegmentSets.SegmentScope.() ->
Class<out SegmentDataNullable<*>>) {
    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        if (headerTypeIndex <= SegmentSets.footerInitIndex)
            throw RuntimeException("header max support count ${SegmentSets.headerCapacity}")

        val viewTypeList: List<Int> = mSegmentsHeader.keys.toList()


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= headerSize())
            newPos = headerSize()

        //before insert data
        val newSegments = mutableMapOf<Int, SegmentInfo<out SegmentDataNullable<*>>>()
        var i = 0
        while (i < newPos) {
            val type: Int = viewTypeList[i]
            newSegments[type] = mSegmentsHeader[type] as SegmentInfo<SegmentDataNullable<*>>
            i++
        }

        //insert data
        val segScope = SegmentSets.SegmentScope()
        val segClass = segScope.func()
        val segInfo = newSegmentInfoDataNullable(segClass)
        if (!segScope.name.isNullOrEmpty())
            segInfo.name = segScope.name
        segInfo.args = segScope.args
        newSegments[headerTypeIndex--] = segInfo

        //after insert data
        i = newPos
        while (i < viewTypeList.size) {
            val type = viewTypeList[i]
            newSegments[type] = mSegmentsHeader[type] as SegmentInfo<SegmentDataNullable<*>>
            i++
        }

        mSegmentsHeader = newSegments
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

        if (mSegmentsHeader.isEmpty())
            return


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= mSegmentsHeader.size)
            newPos = mSegmentsHeader.size - 1

        val type: Int = mSegmentsHeader.keys.toList()[newPos]
        mSegmentsHeader.remove(type)
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
inline fun RecyclerView.footerAdd(pos: Int, func: SegmentSets.SegmentScope.() ->
Class<out SegmentDataNullable<*>>) {
    if (adapter == null)
        return
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        if (footerTypeIndex <= SegmentSets.footerInitIndex - SegmentSets.footerCapacity)
            throw RuntimeException("footer max support count ${SegmentSets.footerCapacity}")

        val viewTypeList = mSegmentsFooter.keys.toList()


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= footerSize())
            newPos = footerSize()

        //before insert data
        val newSegments = mutableMapOf<Int, SegmentInfo<out SegmentDataNullable<*>>>()
        var i = 0
        while (i < newPos) {
            val type: Int = viewTypeList[i]
            newSegments[type] = mSegmentsFooter[type] as SegmentInfo<SegmentDataNullable<*>>
            i++
        }

        //insert data
        val segScope = SegmentSets.SegmentScope()
        val segClass = segScope.func()
        val segInfo = newSegmentInfoDataNullable(segClass)
        if (!segScope.name.isNullOrEmpty())
            segInfo.name = segScope.name
        segInfo.args = segScope.args
        newSegments[footerTypeIndex--] = segInfo

        //after insert data
        i = newPos
        while (i < viewTypeList.size) {
            val type = viewTypeList[i]
            newSegments[type] = mSegmentsFooter[type] as SegmentInfo<SegmentDataNullable<*>>
            i++
        }

        mSegmentsFooter = newSegments
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

        if (mSegmentsFooter.isEmpty())
            return


        //防止错误输入
        var newPos = pos
        if (pos < 0)
            newPos = 0
        if (pos >= mSegmentsFooter.size)
            newPos = mSegmentsFooter.size - 1

        val type: Int = mSegmentsFooter.keys.toList()[newPos]
        mSegmentsFooter.remove(type)
        adapter?.notifyDataSetChanged()
    }

}


/**
 * remove a footer segment to RecyclerView by name
 */
fun RecyclerView.footerRemove(name: String) {
    val index = index(name)
    if (index >= 0)
        footerRemove(index)
}


/**
 * get header or footer index by name,not fount return -1
 * if has same name,return first find
 */
fun RecyclerView.index(name: String): Int {
    if (adapter == null)
        return -1
    if (name.isEmpty())
        return -1
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets

    segmentSets.run {

        //找header
        var i = 0
        mSegmentsHeader.values.forEach {
            if (name == it.name)
                return i
            i++
        }
        //找footer
        i = 0
        mSegmentsFooter.values.forEach {
            if (name == it.name)
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
        var i = 0
        mSegmentsHeader.values.forEach {
            if (name == it.name)
                return it.view
            i++
        }
    }
    "header(name) faild,because not found segment,name=${name}".log()
    return null
}

/**
 * get a footer view by name from RecyclerView
 */
fun RecyclerView.footer(name: String): View? {
    if (adapter == null)
        return null
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    segmentSets.run {
        var i = 0
        mSegmentsFooter.values.forEach {
            if (name == it.name)
                return it.view
            i++
        }
    }
    "footer(name) faild,because not found segment,name=${name}".log()
    return null
}

/**
 * get a header view by position from RecyclerView
 */
fun RecyclerView.header(index: Int): View? {
    if (adapter == null)
        return null
    if (index < 0) {
        "header(index) faild,because index little than 0".log()
        return null
    }
    if (index >= headerSize()) {
        "header(index) faild,because index bigger than header size,header size=${headerSize()},index=$index".log()
        return null
    }
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    val targetViewType = segmentSets.headerIndex2ViewType(index)
    return segmentSets.mSegmentsHeader[targetViewType]?.view
}

/**
 * get a footer view by position from RecyclerView
 */
fun RecyclerView.footer(index: Int): View? {
    if (adapter == null)
        return null
    if (index < 0) {
        "footer(index) faild,because index little than 0".log()
        return null
    }
    if (index >= footerSize()) {
        ("footer(index) faild,because index bigger than footer size,footer size=${footerSize()}," +
                "index=$index").log()
        return null
    }
    val segmentSets = (adapter as RecyclerViewAdpt<*>).segmentSets
    val targetViewType = segmentSets.footerIndex2ViewType(index)
    return segmentSets.mSegmentsFooter[targetViewType]?.view
}