package org.pulp.kotlintext

import android.app.Activity
import android.content.Context
import android.content.Intent


class A {
    fun a() {
        println("A a")
    }

    fun b() {
        println("A b")
    }
}

class B {
    fun b() {
        println("B b")
    }
}


fun A.test(init: B.() -> Unit) {
    val b = B()
    b.init()
}

fun t1(function: (Int, Int) -> Int) {
    val function1 = function(1, 2)
    println(function1)
}

class F {
    var a: String? = null
}

inline fun <reified T> reifiedTest() {
    println(T::class.java)
}

class C {
    var a = "123"

    operator fun get(name: String): String {
        return a
    }
}

class D<T>() {

    fun func(a: T): T {
        return a
    }

}

class Header{
    operator fun get(a:Int):String?{
        return "123"
    }
}


fun header():Header?{
    return Header()
}


fun main() {

    val s = header()?.get(1)

//    with(A()) {
//        test {
//            a()
//            b()
//            this@with.b()
//
//        }
//    }

//    t1 { a, b ->
//        run {
//            val c = 5 * 5
//            a + b + c
//        }
//
//    }


//    F().run {
//
//        a!!.run {
//            println(this)
//
//        }
//
//    }

//    reifiedTest<A>()

//    println(C()["5"])

    val stringList: MutableList<String> = mutableListOf("a", "b", "c", "d")
    val intList: MutableList<Int> = mutableListOf(1, 2, 3, 4)
    printList(stringList)//这里实际上是编译不通过的
    printList(intList)//这里实际上是编译不通过的


}

fun printList(list: MutableList<out Any>) {
//    list.add(3.0f)//开始引入危险操作dangerous! dangerous! dangerous!
    val get = list.get(0)
    list.forEach {
        println(it)
    }
}