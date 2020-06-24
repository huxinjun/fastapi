package org.pulp.fastapi.factory;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.pulp.fastapi.extension.SimpleCallAdapter;
import org.pulp.fastapi.extension.SequenceObservable;
import org.pulp.fastapi.extension.SimpleListObservable;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.util.ULog;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 支持简化调用的call factory(SimpleObservable)
 * 可省略调度线程subscribeOn与observeOn,默认subscribeOn在子线程,observeOn在主线程
 * 可使用链式直接给出成功或失败的回调,失败回调中也包含app自定义的失败(网络请求是成功的)
 * 支持api观察数据类型为String,调试时测试接口不必先解析为实体
 * 使用方法:
 * API声明:SimpleObservable<Object> getServerData(String p1);
 * 请求方式:
 * ApiClient.getApi().getServerData("abc")
 * .success(data -> ULog.out("data:" + data))
 * .faild(error -> ULog.out("error:" + error.getStatus()));
 * <p>
 * Created by xinjun on 2019/12/4 16:28
 */
public class SimpleCallAdapterFactory extends CallAdapter.Factory {

    public static SimpleCallAdapterFactory create() {
        return new SimpleCallAdapterFactory();
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        ULog.out("AichangCallAdapterFactory:rawType=" + rawType);

        if (rawType == SimpleObservable.class
                || rawType == SimpleListObservable.class
                || rawType == SequenceObservable.class
                || rawType == URL.class) {
            RxJava2CallAdapterFactory rxCallAdapterFactory = findRxCallAdapterFactory(retrofit);
            if (rxCallAdapterFactory == null)
                return null;
            Observable<Void> observable = new Observable<Void>() {
                @Override
                protected void subscribeActual(Observer<? super Void> observer) {
                }
            };
            Type genericSuperclass = observable.getClass().getGenericSuperclass();
            if (genericSuperclass == null)
                return null;
            Type observableType;
            try {
                observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
            } catch (ClassCastException ex) {
                //非泛型返回值
                observableType = returnType;
            }
            CallAdapter<?, ?> callAdapter = rxCallAdapterFactory.get(genericSuperclass, annotations, retrofit);
            return new SimpleCallAdapter(callAdapter, observableType, rawType, annotations);
        }
        return null;
    }

    private RxJava2CallAdapterFactory findRxCallAdapterFactory(Retrofit retrofit) {
        if (retrofit == null)
            return null;
        List<CallAdapter.Factory> factories = retrofit.callAdapterFactories();
        if (factories == null || factories.size() == 0)
            return null;
        for (CallAdapter.Factory factory : factories)
            if (factory instanceof RxJava2CallAdapterFactory)
                return (RxJava2CallAdapterFactory) factory;
        return null;
    }
}
