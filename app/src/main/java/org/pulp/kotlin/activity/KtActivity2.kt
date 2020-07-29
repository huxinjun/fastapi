package org.pulp.kotlin.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.pulp.main.R
import org.pulp.viewdsl.*

class KtActivity2 : AppCompatActivity(), View.OnClickListener {

    var mf: MyFinder2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val createView = MyActivityUI2().createView(AnkoContextImpl(this, this, false))
        setContentView(createView)




        mf = finder(MyFinder2(createView)) {

            rcv.safe {
                data { DataTest().arr }
            }

            btn.safe {
                setOnClickListener(this@KtActivity2)
            }

            btn2.safe {
                setOnClickListener(this@KtActivity2)
            }

            btn3.safe {
                setOnClickListener(this@KtActivity2)
            }

            btn4.safe {
                setOnClickListener(this@KtActivity2)
            }

            btn5.safe {
                setOnClickListener(this@KtActivity2)
            }

            btn6?.setOnClickListener(this@KtActivity2)
            btn7?.setOnClickListener(this@KtActivity2)
            btn8?.setOnClickListener(this@KtActivity2)
            btn9?.setOnClickListener(this@KtActivity2)

            rcv.safe {

                dataHeader(0, IT("测试header填充数据", "before templete"))
                dataFooter(0, IT("测试footer填充数据", "before templete"))

                templete {

                    type {
                        when (pos) {
                            0 -> 11
                            1 -> 21
                            else -> 31
                        }
                    }

                    header {
                        name = "header1"
                        SegHeader1(ctx)
                    }
                    header {
                        SegHeader2(ctx)
                    }

                    item(11) {
                        SegItem1(ctx)
                    }

                    item(21) {
                        SegItem2(ctx)
                    }
                    item(31) {
                        SegItem3(ctx)

                    }

                    footer {
                        SegFooter(ctx)
                    }
                    footer {
                        name = "footer2"
                        SegFooter(ctx)
                    }
                }

                dataHeader(1, IT("测试header填充数据", "after templete"))
                dataFooter(1, IT("测试footer填充数据", "after templete"))
            }
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            1000 -> {
                mf?.rcv.safe {
                    headerAdd(88) {
                        SegHeader1(this@KtActivity2)
                    }
                }
            }

            1002 -> {
                mf?.rcv.safe {
                    headerRemove("header1")
                }
            }

            1003 -> {
                mf?.rcv.safe {
                    footerAdd(1) {
                        name = "addfooter"
                        SegFooter(ctx)
                    }
                }
            }

            1004 -> {

                mf?.rcv.safe {
                    footerRemove("footer2")
                }

            }
            1005 -> {
                mf?.rcv.safe {
                    val index = index("addfooter")
                    "find name=addfooter index=$index".log()
                }
            }
            1006 -> {
                mf?.rcv.safe {
                    val header = header(0)
                    header.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by pos"
                    }
                }
            }
            1007 -> {
                mf?.rcv.safe {
                    val footer = footer(0)
                    footer.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by pos"
                    }
                }
            }
            1008 -> {
                mf?.rcv.safe {
                    val header = header("header1")
                    header.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by name"
                    }
                }
            }
            1009 -> {
                mf?.rcv.safe {
                    val footer = footer("footer2")
                    footer.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by name"
                    }
                }
            }
        }
        mf?.rcv.safe {

//            header(0).safe {
//                finder(this) {
//                    find<RecyclerView>(R.id.rcv_inner) {
//                        data {
//
//                            clear()
//                            arrayOf("的测温", "费废物废物", "会议厅局医院").asList()
//                        }
//                    }
//                }
//            }
//            footer(0).safe {
//
//                finder(object : Finder(this) {
//                    @Bind(R.id.tv_txt)
//                    var tv: TextView? = null
//                }) {
//                    tv?.text = "this is new footer text"
//                }
//            }
        }


    }


}

class SegHeader1(ctx: Context) : SegmentDataNullable<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "${data?.name}:${data?.value}"
                }

//                find<RecyclerView>(R.id.rcv_inner) {
//                    templete {
//                        layoutManager = LinearLayoutManager(
//                                ctx,
//                                RecyclerView.HORIZONTAL,
//                                false
//                        )
//
//                        item {
//                            SegHeader1Item(ctx)
//                        }
//
//                    }
//
//                    data { arrayOf("的", "额", "个").asList() }
//                }
            }


        }
    }
}


class SegHeader1Item(ctx: Context) : Segment<Any>() {
    init {
        layout(R.layout.layout_inner_item)
        bind {
            finder.run {
                find<TextView>(R.id.tv_abc).text = data.toString()
            }
        }
    }
}

class SegHeader2(ctx: Context) : SegmentDataNullable<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "${data?.name}:${data?.value}"
                }

                find<RecyclerView>(R.id.rcv_inner) {
                    visibility = View.GONE
                }
            }

        }
    }
}

class SegFooter(ctx: Context) : SegmentDataNullable<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "${data?.name}:${data?.value}"
                }

                find<RecyclerView>(R.id.rcv_inner) {
                    visibility = View.GONE
                }
            }

        }
    }
}


class SegItem1(ctx: Context) : Segment<IT0>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt).text = data?.data + data?.text
            }
        }
    }
}

class SegItem2(ctx: Context) : Segment<IT>() {
    init {
        layout(R.layout.layout2)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt).text = data?.name + data?.value
            }
        }
    }
}

class SegItem3(ctx: Context) : Segment<IT>() {
    init {
        layout(R.layout.layout3)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt).text = data?.name + data?.value
            }
        }
    }
}


class MyFinder2(v: View) : Finder(v) {
    @Bind(1000)
    var btn: Button? = null

    @Bind(1001)
    var rcv: RecyclerView? = null

    @Bind(1002)
    var btn2: Button? = null

    @Bind(1003)
    var btn3: Button? = null

    @Bind(1004)
    var btn4: Button? = null

    @Bind(1005)
    var btn5: Button? = null

    @Bind(1006)
    var btn6: Button? = null

    @Bind(1007)
    var btn7: Button? = null

    @Bind(1008)
    var btn8: Button? = null

    @Bind(1009)
    var btn9: Button? = null

}


class MyActivityUI2 : AnkoComponent<KtActivity2> {
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun createView(ui: AnkoContext<KtActivity2>): View {
        return ui.run {
            verticalLayout {
                button("header Add") {
                    id = 1000
                    onClick { ctx.toast("Hello!") }

                }
                button("header Remove") {
                    id = 1002
                }
                button("footer Add") {
                    id = 1003
                }
                button("footer Remove") {
                    id = 1004
                }
                button("find index by name") {
                    id = 1005
                }
                button("get header view by index") {
                    id = 1006
                }
                button("get footer view by index") {
                    id = 1007
                }
                button("get header view by name") {
                    id = 1008
                }
                button("get footer view by name") {
                    id = 1009
                }

                recyclerView {
                    id = 1001
                    backgroundColor = Color.parseColor("#88dedede")
                    layoutManager = LinearLayoutManager(ui.ctx)
                    overScrollMode = View.OVER_SCROLL_NEVER
                    layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )

                }
            }
        }
    }
}