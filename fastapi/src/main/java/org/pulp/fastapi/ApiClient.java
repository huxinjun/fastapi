package org.pulp.fastapi;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.zhy.http.okhttp.https.HttpsUtils;

import org.pulp.fastapi.anno.BaseUrl;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.factory.SimpleCallAdapterFactory;
import org.pulp.fastapi.factory.SimpleCallFactory;
import org.pulp.fastapi.factory.SimpleConverterFactory;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.life.DestoryHelper;
import org.pulp.fastapi.life.DestoryWatcher;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.util.ChainUtil;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.ULog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
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

    private static ApiClient client;
    private Setting setting;
    private OkHttpClient okHttpClient;
    private Map<String, Retrofit> retrofitMap = new HashMap<>();
    private Cache cache;
    private ReentrantLock lock = new ReentrantLock();

    public static void init(Setting setting) {
        getClient().setting = setting;
        getClient().init();
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
        if (getClient().setting == null)
            throw new RuntimeException("not init,please invoke init method");
        return getClient().createProxyApi(destoryWatcher, apiclass);
    }


    /**
     * 获取api
     * !!!!!!!!!!!!!此方法只适用于获取固定url,configurl,其他的api请求请传递DestoryWatcher对象
     */
    public static <T> T getApi(Class<T> apiclass) {
        if (getClient().setting == null)
            throw new RuntimeException("not init,please invoke init method");
        return getClient().createProxyApi(null, apiclass);
    }


    private ApiClient() {
    }


    /**
     * 初始化OkHttpClient,Retrofit,HttpLog
     */
    private void init() {
        HttpLoggingInterceptor.Logger logger = setting.onCustomLogger() == null ? HttpLoggingInterceptor.Logger.DEFAULT : setting.onCustomLogger();
        //声明日志类
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(logger);
        //设定日志级别
        HttpLoggingInterceptor.Level level = setting.onCustomLoggerLevel();
        level = level != null ? level : HttpLoggingInterceptor.Level.BODY;
        logInterceptor.setLevel(level);

        cache = new Cache(new File(setting.onGetCacheDir(), "CacheFile"), setting.onGetCacheSize());

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .addInterceptor(logInterceptor)
                .addInterceptor(INTERCEPTOR_STATIC_URL_SUPPORT)
                .addInterceptor(INTERCEPTOR_NONET_CACHE_CONTROL)
                .addInterceptor(INTERCEPTOR_PARSER_SUPPORT)
                .cache(cache)
                .connectTimeout(setting.onGetConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(setting.onGetReadTimeout(), TimeUnit.MILLISECONDS);

        okHttpClient = okHttpBuilder.build();
    }

    private static Interceptor INTERCEPTOR_NONET_CACHE_CONTROL = chain -> {
        Request request = chain.request();
        CacheControl cacheControl = request.cacheControl();
        if (!CommonUtil.isConnected(getClient().setting.onGetApplicationContext())) {
            if (!cacheControl.noCache())
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)//只访问缓存
                        .build();
        }
        return setCacheTag(chain.proceed(request));
    };

    private static Interceptor INTERCEPTOR_PARSER_SUPPORT = chain -> {
        Request request = chain.request();
        String extra_anno_parser_before = request.header(InterpreterParseBefore.HEADER_FLAG);
        String extra_anno_parser_error = request.header(InterpreterParseError.HEADER_FLAG);
        String extra_anno_parser_custom = request.header(InterpreterParserCustom.HEADER_FLAG);
        Response response = chain.proceed(request);
        if (!TextUtils.isEmpty(extra_anno_parser_before))
            response = setExtraTag(response, InterpreterParseBefore.HEADER_FLAG, extra_anno_parser_before);
        if (!TextUtils.isEmpty(extra_anno_parser_error))
            response = setExtraTag(response, InterpreterParseError.HEADER_FLAG, extra_anno_parser_error);
        if (!TextUtils.isEmpty(extra_anno_parser_custom))
            response = setExtraTag(response, InterpreterParserCustom.HEADER_FLAG, extra_anno_parser_custom);
        return response;
    };


    private static Interceptor INTERCEPTOR_STATIC_URL_SUPPORT = chain -> {
        Request request = chain.request();
        if (request.header("StaticUrl") != null)
            CommonUtil.throwError(Error.STATIC_URL_TRICK, "this is a trick!");
        return chain.proceed(request);
    };


    /**
     *
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxyApi(DestoryWatcher destoryWatcher, Class<T> apiClass) {
        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(), new Class<?>[]{apiClass},
                (proxy, method, args) -> {

                    //防止多个线程同时请求接口,改变了SimpleCallAdapterFactory中对apiClass的引用
                    lock.tryLock();

                    List<String> baseUrls = findBaseUrls(apiClass, method);
                    String baseUrl = ChainUtil.doChain(false, (obj) -> {
                        if (TextUtils.isEmpty(obj))
                            return null;
                        return obj;
                    }, baseUrls);

                    Retrofit retrofit = retrofitMap.get(baseUrl);
                    if (retrofit == null) {
                        retrofit = new Retrofit.Builder()
                                .baseUrl(baseUrl)//不写会报错，动态域会被 @Url 替换
                                .callFactory(SimpleCallFactory.getInstance(okHttpClient))
                                .addConverterFactory(SimpleConverterFactory.create())
                                .addCallAdapterFactory(SimpleCallAdapterFactory.create())//支持SimpleObservable
                                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//支持rxjava
                                .build();
                        retrofitMap.put(baseUrl, retrofit);
                    }

                    List<CallAdapter.Factory> factories = retrofit.callAdapterFactories();
                    for (CallAdapter.Factory f : factories) {
                        if (f instanceof SimpleCallAdapterFactory)
                            ((SimpleCallAdapterFactory) f).setApiClass(apiClass);
                    }

                    Object invoke = method.invoke(retrofit.create(apiClass), args);
                    ULog.out("createProxyApi.invoke=" + invoke);
                    if (invoke instanceof SimpleObservable) {
                        SimpleObservable simpleObservable = (SimpleObservable) invoke;
                        if (destoryWatcher != null)
                            DestoryHelper.add(destoryWatcher, simpleObservable);
                    }

                    lock.unlock();
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
        return setExtraTag(response, "Cache", "true");
    }

    /**
     * 为了使实体知道自己是否是缓存中的数据
     * 在body中添加cache标记
     */
    private static Response setExtraTag(Response response, String key, String value) {
        if (response == null || response.body() == null)
            return response;
        if (TextUtils.isEmpty(key))
            return response;
        try {
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(),
                    key + "=" + value + SimpleConverterFactory.TAG_EXTRA + response.body().string())).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }


    private List<String> findBaseUrls(Class apiClass, Method apiMethod) {
        List<String> ret = new ArrayList<>();
        BaseUrl baseUrlMethodAnno = apiMethod.getAnnotation(BaseUrl.class);
        if (baseUrlMethodAnno != null)
            ret.add(baseUrlMethodAnno.value());

        BaseUrl baseUrlClassAnno = (BaseUrl) apiClass.getAnnotation(BaseUrl.class);
        if (baseUrlClassAnno != null)
            ret.add(baseUrlClassAnno.value());

        if (!TextUtils.isEmpty(setting.onGetBaseUrl()))
            ret.add(setting.onGetBaseUrl());


        return ret;
    }


    Context getApplicationContext() {
        Context context = getClient().setting.onGetApplicationContext();
        return context instanceof Activity ? context.getApplicationContext() : context;
    }

    static ApiClient getClient() {
        if (client == null) {
            client = new ApiClient();
        }
        return client;
    }

    Setting getSetting() {
        return setting;
    }


    Cache getCache() {
        return cache;
    }
}
