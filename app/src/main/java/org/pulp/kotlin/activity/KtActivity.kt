package org.pulp.kotlin.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.pulp.viewdsl.Bind
import org.pulp.viewdsl.Finder

class KtActivity1 : AppCompatActivity() {

    var mf: MyFinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val createView = MyActivityUI().createView(AnkoContextImpl(this, this, false))
        setContentView(createView)
    }
}


class DataTest {
    var a = "fewfew"
    var headerData = HeaderData()
    var arr = arrayOf(IT0("test", "1"), IT("as", "2"), IT("324", "3")).asList()
//    var arr = arrayOf(1, 2, 3).asList()
}

class IT0(var data: String, var text: String)
class IT(var name: String, var value: String)
class HeaderData() {
    var a = "this is header"
}

class MyFinder(v: View) : Finder(v) {
    @Bind(1000)
    var btn: Button? = null

    @Bind(1002)
    var btn2: Button? = null

    @Bind(1001)
    var rcv: RecyclerView? = null
}


class MyActivityUI : AnkoComponent<KtActivity1> {
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun createView(ui: AnkoContext<KtActivity1>): View {
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
