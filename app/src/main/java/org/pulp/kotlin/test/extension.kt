package cn.xzbenben.test

open class E {

}

open class E1 : E() {

}

open class A {

    open fun E.f() {
        println("E.f in A")
    }

    open fun E1.f() {
        println("E1.f in A")
    }

    fun call(e: E) {
        e.f()
    }
}

class A1 : A() {

    override fun E.f() {
        println("E.f in A1")
    }

    override fun E1.f() {
        println("E1.f in A1")
    }
}

fun Any?.toString(): String {
    if (this == null) return "null"
    // 空检测之后，“this”会自动转换为非空类型，所以下面的 toString()
    // 解析为 Any 类的成员函数
    return toString()
}

fun main(args: Array<String>) {
    // a）
    A().call(E())

    // b）
    A1().call(E())

    // c）
    A().call(E())

    // d）
    A().call(E1())

    //666
    var t = null
    println(t.toString())
}