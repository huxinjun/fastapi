package org.pulp.fastapi.util;


import androidx.annotation.NonNull;

import org.pulp.fastapi.i.InterpreterParseBefore;

import java.util.Arrays;
import java.util.List;

/**
 * 链调用
 * 逐个调用接口集合中的方法
 * 模式一:调用直到返回数据不为null
 * 模式二:下次调用使用上次的返回值,初始时使用传入的参数,直到集合遍历完毕
 */
public class ChainUtil {


    public interface Invoker<R, T, A> {
        R invoke(T obj, A arg);
    }

    public interface BasicInvoker<R, T> {
        R invoke(T obj);
    }


    /**
     * 链式调用,适用于接口方法
     * <p>
     * R,A类型不同时useret必须为false
     *
     * @param useret  是否使用上一环的返回值
     * @param invoker 调用具体实现
     * @param objs    链
     * @param arg     参数,只支持单参数
     * @param <R>     返回值类型
     * @param <T>     链类型
     * @param <A>     参数类型
     * @return 返回值
     */
    @SuppressWarnings("unchecked")
    public static <R, T, A> R doChain(boolean useret, Invoker<R, T, A> invoker, List<T> objs, A arg) {

        R ret = !useret ? null : (R) arg;
        int i = 0;
        do {
            R ret2 = invoker.invoke(objs.get(i), arg);
            ret = ret2 == null ? ret : ret2;
            if (!useret) {
                if (ret != null)
                    return ret;
            } else
                arg = (A) ret;

            i++;

        } while (i < objs.size());


        return ret;

    }

    /**
     * 链式调用,适用于基础类型
     *
     * @param useret  是否使用上一环的返回值
     * @param invoker 调用具体实现
     * @param objs    链
     * @param <R>     返回值类型:String,int等等基础类型
     * @param <T>     链类型
     * @return 返回值
     */
    @SuppressWarnings("unchecked")
    public static <R, T> R doChain(boolean useret, BasicInvoker<R, T> invoker, List<T> objs) {

        R ret = null;
        int i = 0;
        do {
            ret = invoker.invoke(ret == null ? objs.get(i) : (T) ret);
            if (!useret)
                if (ret != null)
                    return ret;
            i++;

        } while (i < objs.size());


        return ret;

    }

    public static void main(String[] args) {
        InterpreterParseBefore i1 = new InterpreterParseBefore() {
            @NonNull
            @Override
            public String onBeforeParse(@NonNull String json) {
                return null;
            }
        };
        InterpreterParseBefore i2 = new InterpreterParseBefore() {
            @NonNull
            @Override
            public String onBeforeParse(@NonNull String json) {
                return json + "bbb";
            }
        };
        InterpreterParseBefore i3 = new InterpreterParseBefore() {
            @NonNull
            @Override
            public String onBeforeParse(@NonNull String json) {
                return null;
            }
        };

        InterpreterParseBefore[] chain = new InterpreterParseBefore[]{i1, i2, i3};


        String ret = doChain(true, new Invoker<String, InterpreterParseBefore, String>() {
            @Override
            public String invoke(InterpreterParseBefore obj, String arg) {
                return obj.onBeforeParse(arg);
            }
        }, Arrays.asList(chain), "first");

        System.out.println(ret);
    }

}
