package org.pulp.fastapi.extension;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;


import org.pulp.fastapi.i.CachePolicy;
import org.pulp.fastapi.Bridge;
import org.pulp.fastapi.factory.SimpleCallFactory;
import org.pulp.fastapi.life.DestoryWatcher;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 可以简化API请求调用的Observable
 * 也可以给请求url追加参数哦
 * 扩展缓存策略(缓存+网络)
 * <p>
 * rxjava链:
 * OkHttpCall
 * CallExecuteObservable
 * BodyObservable
 * IOObservable
 * ObservableSubscribeOn
 * ObservableObserveOn
 * SimpleObservable
 * InternalObserver
 * SimpleObserver
 * <p>
 * Created by xinjun on 2019/12/4 16:38
 */
public class SimpleObservable<T extends IModel> extends Observable<T> implements Disposable, DestoryWatcher.DestoryListener, IOObservable.IORun {


    /**
     * 数据请求成功回调
     * Created by xinjun on 2019/12/9 11:15
     */
    public interface Success<T> {
        void onSuccess(@NonNull T data);
    }

    /**
     * 数据请求成功回调,但不会触发订阅
     * Created by xinjun on 2019/12/9 11:15
     */
    public interface Over {
        void onOver();
    }

    /**
     * 数据请求失败或者服务器返回了错误信息回调
     * Created by xinjun on 2019/12/9 11:16
     */
    public interface Faild {
        void onFaild(@NonNull Error error);
    }


    /**
     * url add extra param
     * Created by xinjun on 2019/12/9 14:46
     * Update: 这里修改modify的返回值为新的request请求。
     */
    interface RequestRebuilder {
        void onModify(Request.Builder builder, Map<String, String> params);
    }

    public static final String TIME_HEADER_FLAG = "TimeUsed";
    private String logTimeTag = null;

    private Type observableType;//api返回类型,用于支持cache use all
    private Annotation[] annotations;//api声明的注解,用于支持cache use all
    private Observable<T> upstream;
    private Over over;
    private Success<T> success;
    private Faild faild;
    private boolean newRequest = true;//用于链式调用只subscrib一次,并且同一个SimpleObservable多次复用
    private boolean abort = false;
    private Map<String, String> extraParam;
    private boolean mIsToastError;
    private InternalObserver mInternalObserver;
    private Handler mHandler;
    private CacheControl cacheControl;//动态切换的缓存策略
    private String cacheControlStr;//动态切换的缓存策略,字符串形式
    private T currData;
    private RequestRebuilder mRequestRebuilder;

    private String path;//与之关联的path
    private AtomicReference<Disposable> atomicReference = new AtomicReference<>();
    private SimpleObserver<T> simpleObserver = new SimpleObserver<>();
    private Retrofit retrofit;
    private Class<?> apiClass;

    SimpleObservable(Observable<T> upstream, Type observableType, Annotation[] annotations, Retrofit retrofit, Class<?> apiClass) {
        this.upstream = upstream;
        this.observableType = observableType;
        this.annotations = annotations;
        this.retrofit = retrofit;
        this.apiClass = apiClass;
        initHandler();
    }

    private void initHandler() {
        if (Looper.myLooper() == Looper.getMainLooper())
            mHandler = new Handler();
        else
            mHandler = new Handler(Looper.getMainLooper());
    }

    class InternalObserver implements Observer<T> {

        private Observer<? super T> observer;
        private String logTimeTag = null;
        private long lastTime = getCurrTime();
        private long startTime = lastTime;

        InternalObserver(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (isDisposed())
                return;
            newRequest = true;
            if (observer != null)
                observer.onSubscribe(d);
            Log.out("onSubscribe");
        }

        @Override
        public void onNext(T t) {
            if (isDisposed())
                return;
            lastTime = startTime;
            logTimeIfNeed("total time used");
            Log.out("onNext.data=" + t);
            setCurrData(t);
            if (t != null) {
                if (SimpleObservable.this.success != null)
                    success.onSuccess(t);
            }
            if (observer != null)
                observer.onNext(t);

        }

        @Override
        public void onError(Throwable e) {
            if (isDisposed())
                return;
            String message = e.getMessage();
            Error error;
            if (!TextUtils.isEmpty(message) && message.startsWith(Error.SYMBOL)) {
                error = Error.str2err(message);
            } else {
                error = new Error();
                if (!CommonUtil.isConnected(Bridge.getContext())) {
                    error.setCode(Error.ERR_NO_NET);
                    error.setMsg(Error.generateErrorMsg(Error.ERR_NO_NET));
                } else {
                    error.setCode(Error.ERR_CRASH);
                    error.setMsg("application error,open logcat to preview Warning log or stack detail:" + message);
                }
            }
            Log.out("onError.message=" + message);
            Log.out("onError.error=" + error);

            if (SimpleObservable.this.faild != null)
                faild.onFaild(error);
            toastErrorIfNeed(error);
            if (observer != null)
                observer.onError(e);
            if (SimpleObservable.this.over != null)
                over.onOver();
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            if (isDisposed())
                return;
            if (SimpleObservable.this.over != null)
                over.onOver();
            if (observer != null)
                observer.onComplete();
        }


        private long getCurrTime() {
            return System.currentTimeMillis();
        }

        private boolean isNeedLogTime() {
            return !TextUtils.isEmpty(logTimeTag);
        }

        private void logTimeIfNeed(String reason) {
            if (isNeedLogTime()) {
                long currTime = getCurrTime();
                int useTime = (int) (currTime - lastTime);
                lastTime = currTime;
                Log.out(logTimeTag + ":" + reason + "=" + useTime + "ms");
            }
        }
    }


    @Override
    public void runInIO() {
        Log.out("RequestWatcher.runInIO=" + Thread.currentThread().getId() + "---" + mInternalObserver);

        mInternalObserver.logTimeIfNeed("create io thread");
        final InternalObserver finalObserver = mInternalObserver;


        SimpleCallFactory.getInstance(null).setRequestWatcher(Thread.currentThread().getId(), new SimpleCallFactory.RequestWatcher() {
            @Override
            public Request onRequestCreated(Request request) {
                Log.out("RequestWatcher.callback=" + Thread.currentThread().getId());

                mInternalObserver.logTimeIfNeed("create request");

                Request.Builder builder = request.newBuilder();
                if (mRequestRebuilder != null)
                    mRequestRebuilder.onModify(builder, extraParam);

                mInternalObserver.logTimeIfNeed("append request param");

                //支持解析打印消耗时间的日志
                if (mInternalObserver.isNeedLogTime()) {
                    String flagb64 = Base64.encodeToString(mInternalObserver.logTimeTag.getBytes(), Base64.DEFAULT).trim().replace("=", "!");
                    builder.addHeader(SimpleObservable.TIME_HEADER_FLAG, flagb64 + ":" + mInternalObserver.getCurrTime());
                }

                applyCacheControl(builder);
                mInternalObserver.logTimeIfNeed("applyCacheControl");


                cacheUseAllSupport(builder, finalObserver);
                mInternalObserver.logTimeIfNeed("cacheUseAllSupport");

                return builder.build();
            }
        });
    }


    /**
     * 应用缓存策略
     */
    private void applyCacheControl(Request.Builder builder) {
        Request request = builder.build();
        Log.out("cachePolicy.url:" + request.url());
        Log.out("cachePolicy.before headers:" + request.headers());
        String cacheHeader = request.header("Cache-Control");
        if (TextUtils.isEmpty(getCacheControl()))
            if (TextUtils.isEmpty(cacheHeader))
                Log.out("cachePolicy.use default cache control");
            else
                Log.out("cachePolicy.use anno cache control:" + cacheHeader);
        else {
            String[] split = getCacheControl().split(":");
            if (split.length > 1) {
                builder.header("Cache-Control", split[1]);
                Log.out("cachePolicy.use dynamic cache control:" + split[1]);
            }
        }
        Log.out("cachePolicy.after headers:" + request.headers());
    }

    /**
     * 支持优先使用缓存,无缓存时再请求网络数据
     *
     * @param observer downstream
     */
    @SuppressWarnings("unchecked")
    private void cacheUseAllSupport(Request.Builder builder, final Observer<? super T> observer) {
        try {
            Request request = builder.build();
            Request.Builder newBuilder = request.newBuilder();
            Log.out("cacheUseAllSupport.request:" + request);

            String header = request.header("Cache-Control");
            Log.out("cacheUseAllSupport.header=" + header);
            if (request.header("Accept-Encoding") == null && request.header("Range") == null)
                newBuilder.header("Accept-Encoding", "gzip");


            boolean forceCache = !TextUtils.isEmpty(header) && header.toLowerCase().contains("all");
            if (Bridge.getCache() != null && forceCache) {
                Field internalCacheField = Bridge.getCache().getClass().getDeclaredField("internalCache");
                internalCacheField.setAccessible(true);
                InternalCache internalCache = (InternalCache) internalCacheField.get(Bridge.getCache());
                Response response = internalCache.get(newBuilder.build());
                Log.out("cacheUseAllSupport.cache response=" + response);

                if (response == null)
                    return;
                if (response.body() == null) {
                    Log.out("cacheUseAllSupport.response.body is null!!!");
                    return;
                }

                Response finalResponse = response;
                okhttp3.Response.Builder responseBuilder = response.newBuilder().request(newBuilder.build());
                if ("gzip".equalsIgnoreCase(response.header("Content-Encoding")) && HttpHeaders.hasBody(response)) {
                    GzipSource responseBody = new GzipSource(response.body().source());
                    responseBuilder.body(new RealResponseBody(response.header("Content-Type"), response.body().contentLength(), Okio.buffer(responseBody)));
                    finalResponse = responseBuilder.build();
                    if (finalResponse.body() == null) {
                        Log.out("cacheUseAllSupport.finalResponse.body is null!!!");
                        return;
                    }
                }

                Converter<ResponseBody, Object> bodyConverter = retrofit.responseBodyConverter(observableType, annotations);
                if (bodyConverter == null) {
                    Log.out("cacheUseAllSupport.bodyConverter is null!!!");
                    return;
                }


                final T cacheData = (T) bodyConverter.convert(finalResponse.body());
                cacheData.setCache(true);
                Log.out("cacheUseAllSupport.cache data=" + cacheData);
                final String beforeCacheControl = getCacheControl();
                Log.out("cacheUseAllSupport.beforeCacheControl=" + beforeCacheControl);
                cachePolicy(CachePolicy.ONLY_NETWORK.getValue());//force change cache policy once for current request
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (observer != null)
                            observer.onNext(cacheData);
                        cachePolicy(beforeCacheControl);//restore on main thread
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 动态切换缓存策略
     *
     * @param cacheControl 新的缓存策略
     * @return this
     */
    public SimpleObservable<T> cachePolicy(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
        Log.out("cachePolicy.newCacheControl:" + cacheControl);
        return this;
    }


    void subscribeIfNeed() {
        Log.out("subscribeIfNeed.isDisposed=" + isDisposed() + ",newRequest=" + newRequest);
        if (isDisposed())
            return;
        if (!newRequest)
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                subscribe(simpleObserver);
            }
        });
        newRequest = false;
    }


    //终止一次代码块后续的请求
    //用于分页没有数据时,不让success等方法发出订阅请求
    void abortOnce() {
        abort = true;
        newRequest = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                newRequest = true;
                abort = false;
            }
        });
    }


    public SimpleObservable<T> refresh() {
        subscribeIfNeed();
        return this;
    }


    protected void toastErrorIfNeed(Error error) {
        if (mIsToastError && !TextUtils.isEmpty(error.getMsg()))
            Toast.makeText(Bridge.getContext(), error.getMsg(), Toast.LENGTH_LONG).show();
    }

    //getter or setter-----------------------------------------------------------------------------------------------------------


    /**
     * set a success callback and subscrobe
     *
     * @param success success callback
     * @return this
     */
    public SimpleObservable<T> success(Success<T> success) {
        this.success = success;
        subscribeIfNeed();
        return this;
    }

    /**
     * set a faild callback and subscrobe
     *
     * @param faild faild callback
     * @return this
     */
    public SimpleObservable<T> faild(Faild faild) {
        this.faild = faild;
        subscribeIfNeed();
        return this;
    }

    /**
     * 设置一个over回调并订阅,over回调在成功或失败后一定会调用
     *
     * @param over over
     * @return this
     */
    public SimpleObservable<T> over(Over over) {
        this.over = over;
        subscribeIfNeed();
        return this;
    }


    /**
     * open show error toast and subscribe to parent
     *
     * @return this
     */
    public SimpleObservable<T> toastError() {
        mIsToastError = true;
        subscribeIfNeed();
        return this;
    }

    /**
     * log api used time
     *
     * @param tag tag
     * @return this
     */
    public SimpleObservable<T> lookTimeUsed(@NonNull String tag) {
        this.logTimeTag = tag;
        subscribeIfNeed();
        return this;
    }

    /**
     * set a success callback,then you can invoke refresh to subscribe to parent
     *
     * @param success success
     * @return this
     */
    public SimpleObservable<T> setSuccess(Success<T> success) {
        this.success = success;
        return this;
    }

    /**
     * set a faild callback,then you can invoke refresh to subscribe to parent
     *
     * @param faild faild
     * @return this
     */
    public SimpleObservable<T> setFaild(Faild faild) {
        this.faild = faild;
        return this;
    }

    /**
     * set a over callback,then you can invoke refresh to subscribe to parent
     *
     * @param over over
     * @return this
     */
    public SimpleObservable<T> setOver(Over over) {
        this.over = over;
        return this;
    }


    /**
     * 动态切换缓存策略
     *
     * @param cacheControlStr 新的缓存策略,字符形式
     * @return this
     */
    public SimpleObservable<T> cachePolicy(String cacheControlStr) {
        this.cacheControlStr = cacheControlStr;
        Log.out("cachePolicy.cacheControlStr:" + cacheControlStr);
        return this;
    }


    /**
     * 动态切换缓存策略
     *
     * @param cachePolicy 新的缓存策略,enum type
     * @return this
     */
    public SimpleObservable<T> cachePolicy(CachePolicy cachePolicy) {
        this.cacheControlStr = cachePolicy.getValue();
        Log.out("cachePolicy.cachePolicy enum:" + cachePolicy);
        return this;
    }


    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        mInternalObserver = new InternalObserver(observer);
        mInternalObserver.logTimeTag = logTimeTag;
        logTimeTag = null;
        Log.out("RequestWatcher.subscribeActual=" + Thread.currentThread().getId() + "," + mInternalObserver);
        mInternalObserver.logTimeIfNeed("create InternalObserver");
        try {
            upstream.subscribe(mInternalObserver);
        } catch (Throwable throwable) {
            mInternalObserver.onError(throwable);
        }
    }


    @Override
    public void onDestory() {
        dispose();
    }

    @Override
    public void dispose() {
        Log.out("dispose,path=" + path);
        //切断数据链
        DisposableHelper.dispose(atomicReference);
        //切断引用
        success = null;
        faild = null;
        currData = null;
    }

    @Override
    public boolean isDisposed() {
        return atomicReference.get() == DisposableHelper.DISPOSED;
    }


    void setRequestRebuilder(RequestRebuilder mRequestRebuilder) {
        this.mRequestRebuilder = mRequestRebuilder;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    void setExtraParam(Map<String, String> extraParam) {
        this.extraParam = extraParam;
    }

    T getCurrData() {
        return currData;
    }

    void setCurrData(T currData) {
        this.currData = currData;
    }

    private String getCacheControl() {
        return cacheControl != null ? cacheControl.toString() : cacheControlStr;
    }

    Faild getFaildCallBack() {
        return faild;
    }

    Handler getHandler() {
        return mHandler;
    }

    protected boolean isAbort() {
        return abort;
    }
}
