package org.pulp.fastapi;


import android.app.Activity;
import android.content.Context;

import com.zhy.http.okhttp.https.HttpsUtils;

import org.pulp.fastapi.extension.StaticUrl;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.factory.AichangCallAdapterFactory;
import org.pulp.fastapi.factory.AichangCallFactory;
import org.pulp.fastapi.factory.AichangConverterFactory;
import org.pulp.fastapi.i.PathConverter;
import org.pulp.fastapi.life.DestoryHelper;
import org.pulp.fastapi.life.DestoryWatcher;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.ULog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * okhttp+retrofit+rxjava实现的网络请求框架
 * 在基础功能上添加了:
 * 根据API中配置的方法注解确定请求URL
 * 简化API调用的方式
 * Created by xinjun on 2019/11/29 16:44
 */
public class ApiClient {

    private Context applicationContext;
    private static ApiClient client;
    private Retrofit retrofit;
    private Cache cache;
    private String cacheDir;//缓存目路
    private PathConverter pathConverter;

    private static void init(Context context, String cachePath, PathConverter pathConverter) {
        getClient().applicationContext = context instanceof Activity ? context.getApplicationContext() : context;
        getClient().cacheDir = cachePath;
        getClient().pathConverter = pathConverter;
    }

    /**
     * 获取api
     *
     * @param destoryWatcher 销毁监听,在任何需要操作ui的网络请求创建时必须传此参数,
     *                       否则会造成activity或fragment销毁后操作UI导致的context为null,崩溃
     * @param apiclass       api 声明类
     * @return api接口类生成的一个Observable实例
     */
    public static <T> T getApi(DestoryWatcher destoryWatcher, Class<T> apiclass) {
        if (getClient().applicationContext == null)
            throw new RuntimeException("not init,please invoke init method");
        return getClient().createProxyApi(destoryWatcher, apiclass, getClient().retrofit.create(apiclass));
    }


    /**
     * 获取api
     * !!!!!!!!!!!!!此方法只适用于获取固定url,configurl,其他的api请求请传递DestoryWatcher对象
     */
    public static <T> T getApi(Class<T> apiclass) {
        if (getClient().applicationContext == null)
            throw new RuntimeException("not init,please invoke init method");
        return getClient().createProxyApi(null, apiclass, getClient().retrofit.create(apiclass));
    }


    private ApiClient() {
    }


    /**
     * 初始化OkHttpClient,Retrofit,HttpLog
     */
    private void init() {
        File httpCacheDirectory = new File(cacheDir, "HttpCache");//这里为了方便直接把文件放在了SD卡根目录的HttpCache中，一般放在context.getCacheDir()中
        int cacheSize = 10 * 1024 * 1024;//设置缓存文件大小为10M
        cache = new Cache(httpCacheDirectory, cacheSize);
        //声明日志类
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        //设定日志级别
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .addInterceptor(logInterceptor)
                .addInterceptor(INTERCEPTOR_NONET_CACHE_CONTROL)
                .cache(cache)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = okHttpBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.baidu.com")//不写会报错，动态域会被 @Url 替换
                .callFactory(AichangCallFactory.getInstance(okHttpClient))
                .addConverterFactory(AichangConverterFactory.create())
                .addCallAdapterFactory(AichangCallAdapterFactory.create())//支持SimpleObservable
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//支持rxjava
                .build();
    }

    private static Interceptor INTERCEPTOR_NONET_CACHE_CONTROL = chain -> {
        Request request = chain.request();
        CacheControl cacheControl = request.cacheControl();
        if (!CommonUtil.isConnected(getClient().applicationContext)) {
            if (!cacheControl.noCache())
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)//只访问缓存
                        .build();
        }
        return setCacheTag(chain.proceed(request));
    };

    private static Interceptor INTERCEPTOR_API_METHOD_ANNO = chain -> {
        Request request = chain.request();
        CacheControl cacheControl = request.cacheControl();
        if (!CommonUtil.isConnected(getClient().applicationContext)) {
            if (!cacheControl.noCache())
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)//只访问缓存
                        .build();
        }
        return setCacheTag(chain.proceed(request));
    };

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxyApi(DestoryWatcher destoryWatcher, Class<T> apiClass, T retrofitApi) {
        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(), new Class<?>[]{apiClass},
                (proxy, method, args) -> {
                    Object invoke = method.invoke(retrofitApi, args);
                    ULog.out("createProxyApi.invoke=" + invoke);
                    if (invoke instanceof SimpleObservable) {
                        SimpleObservable simpleObservable = (SimpleObservable) invoke;
                        if (destoryWatcher != null)
                            DestoryHelper.add(destoryWatcher, simpleObservable);
                    } else if (invoke instanceof StaticUrl) {
                        StaticUrl.assemble((StaticUrl) invoke, method, args);
                    }
                    return invoke;
                });
    }


    /**
     * 为了使实体知道自己是否是缓存中的数据
     * 在body中添加cache标记
     */
    private static Response setCacheTag(Response response) {
        if (response == null)
            return response;
        boolean isCache = response.networkResponse() == null && response.cacheResponse() != null;
        if (!isCache)
            return response;
        if (response.body() == null)
            return response;
        try {
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(),
                    AichangConverterFactory.TAG_CACHE + response.body().string())).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }


    Context getApplicationContext() {
        return getClient().applicationContext;
    }

    static ApiClient getClient() {
        if (client == null) {
            client = new ApiClient();
            client.init();
        }
        return client;
    }

    Cache getCache() {
        return getClient().cache;
    }

    Retrofit getRetrofit() {
        return getClient().retrofit;
    }

    PathConverter getPathConverter() {
        return getClient().pathConverter;
    }

}
