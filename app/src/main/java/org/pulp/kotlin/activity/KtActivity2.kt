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
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.pulp.main.R
import org.pulp.viewdsl.*

class KtActivity2 : AppCompatActivity(), View.OnClickListener {

    var mf: MyFinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val createView = MyActivityUI2().createView(AnkoContextImpl(this, this, false))
        setContentView(createView)




        mf = finder(MyFinder(createView)) {

            rcv.safe {
                data { DataTest().arr }
            }

            btn.safe {
                setOnClickListener(this@KtActivity2)
            }

            rcv.safe {

                templete {

                    type {
                        when (pos) {
                            0 -> 11
                            1 -> 21
                            else -> 31
                        }
                    }

                    header {
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
                }
            }
        }


    }

    override fun onClick(v: View?) {
        mf?.rcv.safe {
            header(0).safe {
                finder(this) {
                    find<RecyclerView>(R.id.rcv_inner) {
                        data { arrayOf("的测温", "费废物废物", "会议厅局医院").asList() }
                    }
                }
            }
            footer(0).safe {
                //                finder(this) {
//                    find<TextView>(R.id.tv_txt) {
//                        text = "this is new footer text"
//                    }
//                }

                finder(object : Finder(this) {
                    @Bind(R.id.tv_txt)
                    var tv: TextView? = null
                }) {
                    tv?.text = "this is new footer text"
                }
            }
        }


    }


}

class SegHeader1(ctx: Context) : Segment<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "this is header 1"
                }

                find<RecyclerView>(R.id.rcv_inner) {
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

class SegHeader2(ctx: Context) : Segment<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "this is header 2"
                }

                find<RecyclerView>(R.id.rcv_inner) {
                    visibility = View.GONE
                }
            }

        }
    }
}

class SegFooter(ctx: Context) : Segment<IT>() {
    init {
        layout(R.layout.layout1)
        bind {
            finder.run {
                find<TextView>(R.id.tv_txt) {
                    text = "this is footer"
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


class MyActivityUI2 : AnkoComponent<KtActivity2> {
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun createView(ui: AnkoContext<KtActivity2>): View {
        return ui.run {
            verticalLayout {
                button("Say Hello") {
                    id = 1000
                    onClick { ctx.toast("Hello!") }

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