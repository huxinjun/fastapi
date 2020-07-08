package org.pulp.viewdsl

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import java.lang.Exception
import java.lang.RuntimeException


@Target(AnnotationTarget.FIELD)//表示可以在函数中使用
@Retention(AnnotationRetention.RUNTIME)//表示运行时注解
annotation class Bind(val id: Int)


/**
 * view finder
 * Created by xinjun on 2020/7/7 10:26
 */
open class Finder(var view: View) {

    constructor(ctx: Context, res: Int) : this(LayoutInflater.from(ctx).inflate(res, null, false))

    private var history = SparseArray<View>()

    fun <T : View> find(id: Int): T {
        @Suppress("UNCHECKED_CAST")
        if (history[id] != null)
            return history[id] as T
        val v: T = view.findViewById(id)
                ?: throw RuntimeException("finder not find any view by id[${id}],you can use method R to find this view:eg 123456.R(R)")
        history.put(id, v)
        return v
    }

    fun <T : View> find(id: Int, function: T.() -> Unit) {
        val find = find<T>(id)
        find.function()
    }

}


inline fun finder(v: View, function: Finder.() -> Unit): Finder {
    val finder = Finder(v)
    finder.function()
    return finder
}

fun <T : Finder> finder(factory: T): T {
    return finder(factory) {}
}

inline fun <T : Finder> finder(factory: T, function: T.() -> Unit): T {
    return factory.init(factory, function)
}

inline fun <T : Finder, D : Any> T.init(declare: D, function: D.() -> Unit): T {

    declare::class.java.declaredFields.forEach {
        it?.run {
            (it::setAccessible)(true)
            val bindAnno = it.getAnnotation(Bind::class.java)
            if (bindAnno == null || bindAnno.id == 0) {
                try {
                    //尝试将根布局装入未声明Bind注解的字段
                    it.set(declare, view)
                } catch (e: Exception) {
                }
                return@forEach
            }

            val view = find<View>(bindAnno.id)
            "finder.view=$view".log()
            "${it.type}".log()
            "${view::class.java}".log()
            try {
                it.set(declare, view)
            } catch (e: Exception) {
                throw RuntimeException("view type different,class[${declare::class.qualifiedName}]" +
                        ",field[${it.name}],java view type[${it.type}],xml view type[${view::class.java}]")
            }

        }
    }
    declare.function()
    return this
}


