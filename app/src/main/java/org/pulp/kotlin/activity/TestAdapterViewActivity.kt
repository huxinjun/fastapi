@file:Suppress("PropertyName")

package org.pulp.kotlin.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import android.widget.TextView
import org.pulp.main.R
import org.pulp.viewdsl.*
import org.pulp.viewdsl.anno.Bind

class TestAdapterViewActivity : AppCompatActivity() {

    var mf: MyFinderListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflate = View.inflate(this, R.layout.activity_dsl_lv, null)
        setContentView(inflate)


        val arr = arrayOf(
                IT0("item", "1"),
                IT0("item", "2"),
                IT0("item", "3")
        ).asList()

        mf = finder(MyFinderListView(inflate)) {

            lv.safe {
                data { arr }
            }

            lv.safe {
                templete {
                    item {
                        SegItem::class.java
                                .withName("SegItem")
                                .withArgs(ctx)
                                .repeatable(true)
                    }
                }
            }


        }

    }
}


class SegItem : Segment<IT0>() {

    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT0>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data.text + bindCtx.data.data
    }
}


class MyFinderListView(v: View) : Finder(v) {


    @Bind(R.id.lv)
    var lv: ListView? = null

}