package org.pulp.viewdsl

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.pulp.viewdsl.anno.Bind
import org.pulp.viewdsl.anno.BindAuto
import org.pulp.viewdsl.anno.BindRoot
import org.pulp.viewdsl.anno.OnClick
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit


/**
 * view finder
 * Created by xinjun on 2020/7/7 10:26
 */
open class Finder(var mView: View) {

    constructor(ctx: Context, res: Int) : this(LayoutInflater.from(ctx).inflate(res, null, false))

    private var history = SparseArray<View>()

    fun <T : View> find(id: Int): T {
        @Suppress("UNCHECKED_CAST")
        if (history[id] != null)
            return history[id] as T
        val v: T = mView.findViewById(id)
                ?: throw RuntimeException("finder not find any view by id[${mView
                        .context.resources.getResourceName(id)}]")
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

    val isAutoBind = declare::class.java.getAnnotation(BindAuto::class.java) != null
    declare::class.java.declaredFields.forEach { field ->
        field?.run {
            isAccessible = true

            val findView: View
            var throwError = true
            val bindAnno = getAnnotation(Bind::class.java)
            if (bindAnno != null)
                findView = find(bindAnno.id)
            else {
                val isRoot = getAnnotation(BindRoot::class.java) != null
                findView = when {
                    isRoot -> mView
                    isAutoBind -> {
                        val identifier = mView.resources.getIdentifier(
                                name,
                                "id",
                                mView.context.packageName)
                        if (identifier <= 0) {
                            "id not find!!!,field is ${name},class is ${declare::class.java.name}"
                                    .log()
                            return@forEach
                        }
                        find(identifier)
                    }
                    else -> {
                        throwError = false
                        mView
                    }
                }
            }

            try {
                set(declare, findView)
            } catch (e: Exception) {
                if (throwError)
                    throwTypeError(declare, this, findView)
            }
            //支持onclick----------------------------------------------------------------
            val onClickAnno = getAnnotation(OnClick::class.java)
            onClickAnno?.let {
                val methodName = it.value
                if (methodName.isEmpty()) {
                    if (declare is View.OnClickListener) {
                        findView.clickInterval(onClickAnno.interval) {
                            declare.onClick(findView)
                        }
                    } else
                        throwOnClickImplementError(declare, this)
                } else {
                    var method: Method? = null
                    var hasArg = true
                    try {
                        method = declare::class.java.getDeclaredMethod(methodName,
                                findView::class.java)
                    } catch (e: NoSuchMethodException) {
                    }
                    try {
                        if (method == null)
                            method = declare::class.java.getDeclaredMethod(methodName, field.type)
                    } catch (e: NoSuchMethodException) {
                    }
                    try {
                        if (method == null)
                            method = declare::class.java.getDeclaredMethod(methodName, View::class.java)
                    } catch (e: NoSuchMethodException) {
                    }
                    try {
                        if (method == null) {
                            method = declare::class.java.getDeclaredMethod(methodName)
                            hasArg = false
                        }
                    } catch (e: NoSuchMethodException) {
                        throwOnClickMethodNotFindError(declare, this, methodName, findView)
                    }

                    findView.clickInterval(onClickAnno.interval) {
                        method?.isAccessible = true
                        if (hasArg)
                            method?.invoke(declare, findView)
                        else
                            method?.invoke(declare)
                    }


                }
                Unit
            }

        }
    }
    declare.function()
    return this
}

@SuppressLint("CheckResult")
fun View.clickInterval(milliSecond: Long, listener: () -> Unit) {
    if (milliSecond <= 0) {
        this.setOnClickListener { listener() }
        return
    }
    ViewClickObservable(this)
            .throttleFirst(milliSecond, TimeUnit.MILLISECONDS)
            .subscribe { listener() }
}


internal class ViewClickObservable(private val view: View) : Observable<Any?>() {
    override fun subscribeActual(observer: Observer<in Any?>) {
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.setOnClickListener(listener)
    }

    internal class Listener(private val view: View, private val observer: Observer<in Any?>)
        : MainThreadDisposable(), View.OnClickListener {
        override fun onClick(v: View) {
            if (!this.isDisposed) {
                observer.onNext(view)
            }
        }

        override fun onDispose() {
            view.setOnClickListener(null as View.OnClickListener?)
        }

    }

}


//throw errors--------------------------------------------------------------------------------------
fun Finder.throwTypeError(declareObj: Any,
                          field: Field,
                          view: View
) {
    throw RuntimeException("view type different," +
            "class[${declareObj::class.qualifiedName}]," +
            "field[${field.name}],java view type[${field.type}],xml view type[${view::class.java}]")
}

fun Finder.throwOnClickImplementError(declareObj: Any,
                                      field: Field
) {
    throw RuntimeException("when your field[${field.name}] OnClick annotation not set value to " +
            "appoint a method,your class[${declareObj::class.qualifiedName}] " +
            "must implements View.OnClickListener")
}

fun Finder.throwOnClickMethodNotFindError(declareObj: Any,
                                          field: Field,
                                          methodName: String,
                                          findView: View
) {
    throw RuntimeException("not find method[${methodName}] in class[${declareObj::class
            .qualifiedName}] field[${field.name}] ,if has this method,please ensure argument type" +
            " in this collection(\n1.${findView::class.java},\n2.${field.type},\n3.${View::class
                    .java}),or empty argument")
}


