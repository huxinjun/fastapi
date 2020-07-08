package a.b.c.d

import kotlin.properties.Delegates

class UserT {
    var name: String by Delegates.observable("初始值") { prop, old, new ->
        println("旧值：$old -> 新值：$new")
    }


    val a: String by lazy {

        println("init")
        "abc"
    }

    fun <T> sasa(abc: () -> T): T {
        return abc.invoke()
    }


    companion object {
        // lambda
        inline fun test(b: (num1: Int) -> Int) {
            b.invoke(3)
        }
    }
}

fun test(b: (num1: Int) -> Int): Int {
    return b.invoke(3)
}

fun hasZeros(ints: List<Int>): Boolean {
    ints.forEach {
        print("")
        if (it == 0)
            return@forEach // returns from hasZeros
    }
    return false
}

class HTML {
    fun body() {
        println("body")
    }
}

class T {


    companion object {
        fun html(init: HTML.(p: String) -> Unit): HTML {
            println(this)
            println(init)
            val html = HTML()  // 创建接收者对象
            init(html, "init")        // 将该接收者对象传给该 lambda
            return html
        }
    }
}

fun abc(a: Int, b: Int): Int {
    return a * b
}

fun abc2(a: Int, b: Int) = a * b


class Edit {

}


inline fun editText(init: Edit.() -> Unit): Edit {
    println("0")
    return ankoView({ Edit() }, { init() })
}


inline fun <T> ankoView(factory: (ctx: String) -> T, init: T.() -> Unit): T {
    val view = factory("")
    println("b")
    view.init()
    println("c")
    return view
}


class funclass(function: funclass.() -> Unit) {

    var list: MutableList<String> = mutableListOf()

    init {
        function()
        println(list)
    }

    fun add(s: String) {
        println("add$s")
        list.add(s)
    }

    fun remove(s: String) {
        println("remove$s")
        list.remove(s)
    }


}


fun <T> Collection<T>.joinToString(
    separator: String = " ",
    prefix: String = "[",
    postfix: String = "]"
): String {
    val sb = StringBuilder(prefix)
    for ((index, element) in this.withIndex()) {
        if (index > 0) sb.append(separator)
        sb.append(element)
    }

    println(this)
    val stream = isEmpty()
    println(stream)


    sb.append(postfix)
    return sb.toString()
}

fun main(args: Array<String>) {
//    funclass {
//        add("1")
//        add("2")
//        add("3")
//        remove("2")
//    }


//    val user = UserT()
//    user.a = ";;;"
//    print(user.a)

//    user.sasa {
//        var a:Int=0
//        a+=1
//    }

//    println(user.a)
//    println(user.a)
//
//    hasZeros(listOf(1, 2, 3))
//    val test = test {
//        print(it)
//        20
//    }
//
//    println(test)


//    T.html {       // 带接收者的 lambda 由此开始
//        println(this)
//        body()   // 调用该接收者对象的一个方法
//    }

//    val iop = fun Int.(other: Int): Int { return this + other }
//    println(2.iop(3))


//    println(abc(1, 2))
//    println(abc2(1, 2))

//    editText {
//        println("d")
//    }


//    val list = arrayListOf("10", "11", "1001")
//    println(list.joinToString())

    val typeBlock: (ADF.() -> Int)? = {
        println("123")
        0
    }

    typeBlock?.let {
        ADF().it()
    }

    ADF().let {

    }
}

public fun <T, R> T.let(block: (T) -> R): R {
    return block(this)
}

class ADF {}



