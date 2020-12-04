@file:Suppress("PropertyName")

package org.pulp.kotlin.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.pulp.main.R
import org.pulp.viewdsl.*

class KtActivity2 : AppCompatActivity() {

    var mf: MyFinder2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val createView = MyActivityUI2().createView(AnkoContextImpl(this, this, false))
//        setContentView(createView)

        val inflate = View.inflate(this, R.layout.activity_dsl, null)
        setContentView(inflate)



        mf = finder(MyFinder2(inflate)) {

            ("finder.rcv=$rcv").log()
            rcv.safe {
                data { DataTest().arr }
            }


            rcv.safe {
                layoutManager = LinearLayoutManager(context)

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
                        SegHeader1()
                    }
                    header {
                        SegHeader2(ctx)
                    }

//                    footer {
//                        SegFooter(ctx)
//                    }
//                    footer {
//                        name = "footer2"
//                        SegFooter(ctx)
//                    }

                    item(11) {
                        SegItem1(ctx)
                    }

                    item(21) {
                        SegItem2(ctx)
                    }
                    item(31) {
                        SegItem3(ctx)

                    }


                }

                dataHeader(1, IT("测试header填充数据", "after templete"))
                dataFooter(1, IT("测试footer填充数据", "after templete"))
            }
        }


    }


    fun onBtnClick(view: View) {

        when (view.id) {
            R.id.btn_1 -> {
                mf?.rcv.safe {
                    headerAdd(0) {
                        SegHeader1()
                    }
                }
            }

            R.id.btn_2 -> {
                mf?.rcv.safe {
                    headerRemove(88)
                }
            }

            R.id.btn_3 -> {
                mf?.rcv.safe {
                    footerAdd(0) {
                        name = "addfooter"
                        SegFooter()
                    }
                }
            }

            R.id.btn_4 -> {

                mf?.rcv.safe {
                    footerRemove(88)
                }

            }
            R.id.btn_5 -> {
                mf?.rcv.safe {
                    val index = index("addfooter")
                    "find name=addfooter index=$index".log()
                }
            }
            R.id.btn_6 -> {
                mf?.rcv.safe {
                    val header = header(0)
                    header.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by pos"
                    }
                }
            }
            R.id.btn_7 -> {
                mf?.rcv.safe {
                    val footer = footer(0)
                    footer.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find footer view by pos"
                    }
                }
            }
            R.id.btn_8 -> {
                mf?.rcv.safe {
                    val header = header("header1")
                    header.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find header view by name"
                    }
                }
            }
            R.id.btn_9 -> {
                mf?.rcv.safe {
                    val footer = footer("footer2")
                    footer.safe {
                        findViewById<TextView>(R.id.tv_txt).text = "find footer view by name"
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

class SegHeader1 : SegmentDataNullable<IT>() {

    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    @Bind(R.id.rcv_inner)
    lateinit var rcv_inner: RecyclerView

    override fun onCreateView() = R.layout.layout1

    override fun onBind(bindCtx: BindingContextDataNullable<IT>) {
        bindCtx.run {
            tv_txt.text = "${data?.name}:${data?.value}"
            rcv_inner.safe {
                templete {
                    layoutManager = LinearLayoutManager(
                            ctx,
                            RecyclerView.HORIZONTAL,
                            false
                    )

                    item {
                        SegHeader1Item(ctx)
                    }

                }

                data { arrayOf("的", "额", "个").asList() }
            }
        }

    }

}

//class SegHeader1(ctx: Context) : SegmentDataNullable<IT>() {
//    init {
//        layout(R.layout.layout1)
//        bind {
//            finder.run {
//                find<TextView>(R.id.tv_txt) {
//                    text = "${data?.name}:${data?.value}"
//                }
//
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
//            }
//
//
//        }
//    }
//}

class SegHeader1Item(private val ctx: Context) : Segment<Any>() {

    @Bind(R.id.tv_abc)
    lateinit var tv_abc: TextView


    override fun onCreateView(): Int {
        return R.layout.layout_inner_item
    }

    override fun onBind(bindCtx: BindingContext<Any>) {
        tv_abc.text = bindCtx.data.toString()
    }
}

//class SegHeader1Item(ctx: Context) : Segment<Any>() {
//    init {
//
//        layout(R.layout.layout_inner_item)
//        bind {
//            finder.run {
//                find<TextView>(R.id.tv_abc).text = data.toString()
//            }
//        }
//    }
//
//}

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

class SegFooter() : SegmentDataNullable<IT>() {
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
    @Bind(R.id.btn_1)
    var btn: Button? = null

    @Bind(R.id.btn_2)
    var btn2: Button? = null

    @Bind(R.id.btn_3)
    var btn3: Button? = null

    @Bind(R.id.btn_4)
    var btn4: Button? = null

    @Bind(R.id.btn_5)
    var btn5: Button? = null

    @Bind(R.id.btn_6)
    var btn6: Button? = null

    @Bind(R.id.btn_7)
    var btn7: Button? = null

    @Bind(R.id.btn_8)
    var btn8: Button? = null

    @Bind(R.id.btn_9)
    var btn9: Button? = null


    @Bind(R.id.rcv)
    var rcv: RecyclerView? = null

}

//
//class MyActivityUI2 : AnkoComponent<KtActivity2> {
//    @SuppressLint("SetTextI18n", "ResourceType")
//    override fun createView(ui: AnkoContext<KtActivity2>): View {
//        return ui.run {
//            verticalLayout {
//                button("header Add") {
//                    id = 1000
//                    onClick { ctx.toast("Hello!") }
//
//                }
//                button("header Remove") {
//                    id = 1002
//                }
//                button("footer Add") {
//                    id = 1003
//                }
//                button("footer Remove") {
//                    id = 1004
//                }
//                button("find index by name") {
//                    id = 1005
//                }
//                button("get header view by index") {
//                    id = 1006
//                }
//                button("get footer view by index") {
//                    id = 1007
//                }
//                button("get header view by name") {
//                    id = 1008
//                }
//                button("get footer view by name") {
//                    id = 1009
//                }
//
//
//                recyclerView {
//                    id = 1001
//                    backgroundColor = Color.parseColor("#88dedede")
//                    layoutManager = LinearLayoutManager(ui.ctx)
//                    overScrollMode = View.OVER_SCROLL_NEVER
//                    layoutParams = ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//
//                }
//            }
//        }
//    }
//}

class DataTest {
    var a = "fewfew"
    var headerData = HeaderData()
    var arr = arrayOf(
            IT0("item", "1"),
            IT("item", "2"),
            IT("item", "3"),
            IT("item", "4"),
            IT("item", "5")
    ).asList()
}

class HeaderData() {
    var a = "this is header"
}

class IT0(var data: String, var text: String)
class IT(var name: String, var value: String)