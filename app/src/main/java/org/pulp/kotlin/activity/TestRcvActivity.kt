@file:Suppress("PropertyName")

package org.pulp.kotlin.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.pulp.main.R
import org.pulp.viewdsl.*
import org.pulp.viewdsl.anno.Bind
import org.pulp.viewdsl.anno.BindAuto
import org.pulp.viewdsl.anno.BindRoot
import org.pulp.viewdsl.anno.OnClick

class TestRcvActivity : AppCompatActivity() {

    var mf: MyFinder2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val createView = MyActivityUI2().createView(AnkoContextImpl(this, this, false))
//        setContentView(createView)

        val inflate = View.inflate(this, R.layout.activity_dsl, null)
        setContentView(inflate)



        mf = finder(MyFinder2(inflate, this)) {

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
                        SegHeader1::class.java.withName("header1").withArgs(ctx)
                    }
                    header {
                        SegHeader2::class.java
                    }

                    footer {
                        SegFooter::class.java
                    }
                    footer {
                        SegFooter::class.java.withName("footer2")
                    }

                    item(11) {
                        SegItem1::class.java
                    }

                    item(21) {
                        SegItem2::class.java
                    }
                    item(31) {
                        SegItem3::class.java

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
                        SegHeader1::class.java
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
                        SegFooter::class.java
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

    var ctx: Context? = null

    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    @Bind(R.id.rcv_inner)
    lateinit var rcv_inner: RecyclerView

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        "SegHeader1.arg:${ctx}".log()
    }

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
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
                        SegHeader1Item::class.java
                    }

                }

                data { arrayOf("的", "额", "个").asList() }
            }
            rcv_inner.let { }
        }

    }

    override fun onReceiveArg(args: Array<out Any>) {
        super.onReceiveArg(args)
        if (args.isNotEmpty())
            ctx = castCtx(args[0])
    }
}

fun castCtx(any: Any): Context? {
    return if (any is Context) any else null
}

class SegHeader1Item : Segment<Any>() {

    @Bind(R.id.tv_abc)
    lateinit var tv_abc: TextView


    override fun onCreateView(): Int {
        return R.layout.layout_inner_item
    }

    override fun onBind(bindCtx: BindingContext<Any>) {
        tv_abc.text = bindCtx.data.toString()
    }
}

class SegHeader2 : SegmentDataNullable<IT>() {

    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    @Bind(R.id.rcv_inner)
    lateinit var rcv_inner: RecyclerView

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContextDataNullable<IT>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data?.name + bindCtx.data?.value
        rcv_inner.visibility = View.GONE
    }
}

class SegFooter : SegmentDataNullable<IT>() {

    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    @Bind(R.id.rcv_inner)
    lateinit var rcv_inner: RecyclerView

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContextDataNullable<IT>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data?.name + bindCtx.data?.value
        rcv_inner.visibility = View.GONE
    }
}

@BindAuto
class SegItem1 : Segment<IT0>() {

    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout1

    override fun onReceiveArg(args: Array<out Any>) {
        super.onReceiveArg(args)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT0>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data.text + bindCtx.data.data
    }
}

class SegItem2 : Segment<IT>() {
    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout2

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data.name + bindCtx.data.value
    }
}

class SegItem3 : Segment<IT>() {
    @Bind(R.id.tv_txt)
    @OnClick("onTextClick")
    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout3

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT>) {
        super.onBind(bindCtx)
        "SegItem3:${this}".log()
        tv_txt.text = bindCtx.data.name + bindCtx.data.value
    }

    fun onTextClick() {
        getData {
            "SegItem3:${data.name}".log()
        }
    }
}


@BindAuto
class MyFinder2(v: View, private val testRcvActivity: TestRcvActivity) : Finder(v), View.OnClickListener {

    @BindRoot
    lateinit var root: View

    @OnClick("onBtn1Click", 1000)
    lateinit var btn_1: Button

    @OnClick
    var btn_2: Button? = null

    @OnClick
    var btn_3: Button? = null

    @OnClick
    var btn_4: Button? = null

    @OnClick
    var btn_5: Button? = null

    @OnClick
    var btn_6: Button? = null

    @OnClick
    var btn_7: Button? = null

    @OnClick
    var btn_8: Button? = null

    @OnClick
    var btn_9: Button? = null

    var rcv: RecyclerView? = null

    override fun onClick(v: View?) {
        v?.context?.let {
            testRcvActivity.onBtnClick(v)
        }
    }

    fun onBtn1Click() {
        testRcvActivity.onBtnClick(btn_1)
    }

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
            IT("item", "5"),
            IT("item", "6"),
            IT("item", "7"),
            IT("item", "8")
    ).asList()
}

class HeaderData() {
    var a = "this is header"
}

class IT0(var data: String, var text: String)
class IT(var name: String, var value: String)