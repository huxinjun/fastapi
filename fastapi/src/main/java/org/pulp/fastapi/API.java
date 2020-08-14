package org.pulp.fastapi;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;


import com.zhy.http.okhttp.https.HttpsUtils;

import org.pulp.fastapi.anno.BaseUrl;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.factory.SimpleCallAdapterFactory;
import org.pulp.fastapi.factory.SimpleCallFactory;
import org.pulp.fastapi.factory.SimpleConverterFactory;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.life.ActivityLifeWatcher;
import org.pulp.fastapi.life.Bridge;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.util.ChainUtil;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
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
 * 创建请求入口
 * Created by xinjun on 2019/11/29 16:44
 */
public class API {

    private static API client;
    private Setting setting;
    private OkHttpClient okHttpClient;
    private Map<String, Retrofit> retrofitMap = new HashMap<>();
    private Cache cache;
    private ReentrantLock lock = new ReentrantLock();

    public static void init(@NonNull Setting setting) {
        getClient().setting = setting;
        getClient().init();
    }

    /**
     * 获取api
     */
    public static <T> T get(Activity activity, @NonNull Class<T> apiclass) {
        if (getClient().setting == null)
            throw new RuntimeException("not init,please invoke init method");
        return getClient().createProxyApi(activity, apiclass);
    }


    private API() {
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

        if (!(setting.onGetApplicationContext() instanceof Application))
            throw new RuntimeException("Setting need return a Application at Setting.onGetApplicationContext()");
        Application app = (Application) setting.onGetApplicationContext();
        app.registerActivityLifecycleCallbacks(new ActivityLifeWatcher());
    }

    private static Interceptor INTERCEPTOR_NONET_CACHE_CONTROL = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            CacheControl cacheControl = request.cacheControl();
            if (!CommonUtil.isConnected(getClient().setting.onGetApplicationContext())) {
                if (!cacheControl.noCache())
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)//只访问缓存
                            .build();
            }
            return setCacheTag(chain.proceed(request));
        }
    };

    private static Interceptor INTERCEPTOR_PARSER_SUPPORT = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String extra_anno_parser_before = request.header(InterpreterParseBefore.HEADER_FLAG);
            String extra_anno_parser_error = request.header(InterpreterParseError.HEADER_FLAG);
            String extra_anno_parser_custom = request.header(InterpreterParserCustom.HEADER_FLAG);
            String extra_anno_parser_after = request.header(InterpreterParserAfter.HEADER_FLAG);
            String extra_time = request.header(SimpleObservable.TIME_HEADER_FLAG);
            Response response = chain.proceed(request);
            StringBuilder builder = new StringBuilder();

            if (!TextUtils.isEmpty(extra_anno_parser_before))
                builder.append(InterpreterParseBefore.HEADER_FLAG).append("=").append(extra_anno_parser_before).append(SimpleConverterFactory.TAG_EXTRA);
            if (!TextUtils.isEmpty(extra_anno_parser_error))
                builder.append(InterpreterParseError.HEADER_FLAG).append("=").append(extra_anno_parser_error).append(SimpleConverterFactory.TAG_EXTRA);
            if (!TextUtils.isEmpty(extra_anno_parser_custom))
                builder.append(InterpreterParserCustom.HEADER_FLAG).append("=").append(extra_anno_parser_custom).append(SimpleConverterFactory.TAG_EXTRA);
            if (!TextUtils.isEmpty(extra_anno_parser_after))
                builder.append(InterpreterParserAfter.HEADER_FLAG).append("=").append(extra_anno_parser_after).append(SimpleConverterFactory.TAG_EXTRA);
            if (!TextUtils.isEmpty(extra_time))
                builder.append(SimpleObservable.TIME_HEADER_FLAG).append("=").append(extra_time).append(SimpleConverterFactory.TAG_EXTRA);
            if (!TextUtils.isEmpty(builder))
                response = setExtraTag(response, builder.toString());
            return response;
        }
    };


    private static Interceptor INTERCEPTOR_STATIC_URL_SUPPORT = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (request.header("StaticUrl") != null)
                CommonUtil.throwError(Error.Code.STATIC_URL_TRICK.code, "this is a trick!");
            return chain.proceed(request);
        }
    };


    /**
     *
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxyApi(@NonNull final Activity activity, final Class<T> apiClass) {
        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(), new Class<?>[]{apiClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        //防止多个线程同时请求接口,改变了SimpleCallAdapterFactory中对apiClass的引用
                        lock.tryLock();

                        List<String> baseUrls = findBaseUrls(apiClass, method);
                        String baseUrl = ChainUtil.doChain(false, new ChainUtil.BasicInvoker<String, String>() {
                            @Override
                            public String invoke(String obj) {
                                if (TextUtils.isEmpty(obj))
                                    return null;
                                return obj;
                            }
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
                        Log.out("createProxyApi.invoke=" + invoke);
                        if (invoke instanceof SimpleObservable) {
                            SimpleObservable simpleObservable = (SimpleObservable) invoke;
                            Bridge.addDestoryListener(activity, simpleObservable);
                        }

                        lock.unlock();
                        return invoke;
                    }
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

    private static Response setExtraTag(Response response, String tag) {
        if (response == null || response.body() == null)
            return response;
        if (TextUtils.isEmpty(tag))
            return response;
        try {
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(), tag + response.body().string())).build();
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

    static API getClient() {
        if (client == null) {
            client = new API();
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
