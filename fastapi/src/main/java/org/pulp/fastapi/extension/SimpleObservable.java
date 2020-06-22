package org.pulp.fastapi.extension;

import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;


import org.pulp.fastapi.CachePolicy;
import org.pulp.fastapi.Get;
import org.pulp.fastapi.factory.AichangCallFactory;
import org.pulp.fastapi.life.DestoryWatcher;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.ULog;

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
        void onSuccess(T data);
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
        void onFaild(Error error);
    }


    /**
     * url add extra param
     * Created by xinjun on 2019/12/9 14:46
     * Update: 这里修改modify的返回值为新的request请求。
     */
    interface ModifyUrlCallback {
        void onModify(Request.Builder builder, Map<String, String> params);
    }

    private Type observableType;//api返回类型,用于支持cache use all
    private Annotation[] annotations;//api声明的注解,用于支持cache use all
    private Observable<T> upstream;
    private Over over;
    private Success<T> success;
    private Faild faild;
    private boolean newRequest = true;//用于链式调用只subscrib一次,并且同一个SimpleObservable多次复用
    private Map<String, String> extraParam;
    private boolean mIsToastError;
    private InternalObserver mInternalObserver;
    private Handler mHandler = new Handler();
    private CacheControl cacheControl;//动态切换的缓存策略
    private String cacheControlStr;//动态切换的缓存策略,字符串形式
    private T currData;
    private ModifyUrlCallback mModifyUrlCallback;

    private String path;//与之关联的path
    private AtomicReference<Disposable> atomicReference = new AtomicReference<>();
    private SimpleObserver<T> simpleObserver = new SimpleObserver<>();

    SimpleObservable(Observable<T> upstream, Type observableType, Annotation[] annotations) {
        this.upstream = upstream;
        this.observableType = observableType;
        this.annotations = annotations;
    }

    class InternalObserver implements Observer<T> {

        private Observer<? super T> observer;

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
            ULog.out("onSubscribe");
        }

        @Override
        public void onNext(T t) {
            if (isDisposed())
                return;
            ULog.out("onNext.data=" + t);
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
            if (!TextUtils.isEmpty(message) && message.startsWith(Error.Companion.getSYMBOL())) {
                error = Error.Companion.str2err(message);
            } else {
                error = new Error();
                if (!CommonUtil.isConnected(Get.getContext())) {
                    error.setCode(Error.ERR_NO_NET);
                    error.setStatus("no network");
                    error.setMsg("没网了");
                } else {
                    error.setCode(Error.ERR_APP);
                    error.setStatus("application error,open logcat to preview Warning log or stack detail");
                    error.setMsg(message);
                }
            }
            ULog.out("onError.message=" + message);
            ULog.out("onError.error=" + error);
            if (SimpleObservable.this.faild != null)
                faild.onFaild(error);
            assert error != null;
            if (mIsToastError)
                Toast.makeText(Get.getContext()
                        , TextUtils.isEmpty(error.getDesc()) ? error.getMsg() : error.getDesc(), Toast.LENGTH_LONG).show();
            if (observer != null)
                observer.onError(e);
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
    }


    @Override
    public void runInIO() {
        ULog.out("RequestWatcher.runInIO=" + Thread.currentThread().getId());
        AichangCallFactory.getInstance(null).setRequestWatcher(Thread.currentThread().getId(), request -> {
            ULog.out("RequestWatcher.callback=" + Thread.currentThread().getId());
            Request.Builder builder = request.newBuilder();
            if (mModifyUrlCallback != null)
                mModifyUrlCallback.onModify(builder, extraParam);
            applyCacheControl(builder);
            cacheUseAllSupport(builder, mInternalObserver);
            return builder.build();
        });
    }

    /**
     * 应用缓存策略
     */
    private void applyCacheControl(Request.Builder builder) {
        Request request = builder.build();
        ULog.out("cachePolicy.url:" + request.url());
        ULog.out("cachePolicy.before headers:" + request.headers());
        String cacheHeader = request.header("Cache-Control");
        if (TextUtils.isEmpty(getCacheControl()))
            if (TextUtils.isEmpty(cacheHeader))
                ULog.out("cachePolicy.use default cache control");
            else
                ULog.out("cachePolicy.use anno cache control:" + cacheHeader);
        else {
            String[] split = getCacheControl().split(":");
            if (split.length > 1) {
                builder.header("Cache-Control", split[1]);
                ULog.out("cachePolicy.use dynamic cache control:" + split[1]);
            }
        }
        ULog.out("cachePolicy.after headers:" + request.headers());
    }

    /**
     * 支持优先使用缓存,无缓存时再请求网络数据
     *
     * @param observer downstream
     */
    @SuppressWarnings("unchecked")
    private void cacheUseAllSupport(Request.Builder builder, Observer<? super T> observer) {
        try {
            Request request = builder.build();
            Request.Builder newBuilder = request.newBuilder();
            ULog.out("cacheUseAllSupport.request:" + request);

            String header = request.header("Cache-Control");
            ULog.out("cacheUseAllSupport.header=" + header);
            if (request.header("Accept-Encoding") == null && request.header("Range") == null)
                newBuilder.header("Accept-Encoding", "gzip");


            boolean forceCache = !TextUtils.isEmpty(header) && header.toLowerCase().contains("all");
            if (Get.getCache() != null && forceCache) {
                Field internalCacheField = Get.getCache().getClass().getDeclaredField("internalCache");
                internalCacheField.setAccessible(true);
                InternalCache internalCache = (InternalCache) internalCacheField.get(Get.getCache());
                Response response = internalCache.get(newBuilder.build());
                ULog.out("cacheUseAllSupport.cache response=" + response);

                if (response == null)
                    return;
                if (response.body() == null) {
                    ULog.out("cacheUseAllSupport.response.body is null!!!");
                    return;
                }

                Response finalResponse = response;
                okhttp3.Response.Builder responseBuilder = response.newBuilder().request(newBuilder.build());
                if ("gzip".equalsIgnoreCase(response.header("Content-Encoding")) && HttpHeaders.hasBody(response)) {
                    GzipSource responseBody = new GzipSource(response.body().source());
                    responseBuilder.body(new RealResponseBody(response.headers(), Okio.buffer(responseBody)));
                    finalResponse = responseBuilder.build();
                    if (finalResponse.body() == null) {
                        ULog.out("cacheUseAllSupport.finalResponse.body is null!!!");
                        return;
                    }
                }

                Retrofit retrofit = Get.getRetrofit();
                Converter<ResponseBody, Object> bodyConverter = retrofit.responseBodyConverter(observableType, annotations);
                if (bodyConverter == null) {
                    ULog.out("cacheUseAllSupport.bodyConverter is null!!!");
                    return;
                }


                T cacheData = (T) bodyConverter.convert(finalResponse.body());
                cacheData.setCache(true);
                ULog.out("cacheUseAllSupport.cache data=" + cacheData);
                String beforeCacheControl = getCacheControl();
                ULog.out("cacheUseAllSupport.beforeCacheControl=" + beforeCacheControl);
                cachePolicy(CachePolicy.ONLY_NETWORK);//force change cache policy once for current request
                mHandler.post(() -> {
                    if (observer != null)
                        observer.onNext(cacheData);
                    cachePolicy(beforeCacheControl);//restore on main thread
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
        ULog.out("cachePolicy.newCacheControl:" + cacheControl);
        return this;
    }


    void subscribeIfNeed() {
        ULog.out("subscribeIfNeed.isDisposed=" + isDisposed() + ",newRequest=" + newRequest);
        if (isDisposed())
            return;
        if (!newRequest)
            return;
        mHandler.post(() -> subscribe(simpleObserver));
        newRequest = false;
    }

    //终止一次代码块后续的请求
    //用于分页没有数据时,不让success等方法发出订阅请求
    void abortOnce() {
        newRequest = false;
        mHandler.post(() -> newRequest = true);
    }


    public SimpleObservable<T> refresh() {
        subscribeIfNeed();
        return this;
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
        ULog.out("cachePolicy.cacheControlStr:" + cacheControlStr);
        return this;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        mInternalObserver = new InternalObserver(observer);
        upstream.subscribe(mInternalObserver);
    }


    @Override
    public void onDestory() {
        dispose();
    }

    @Override
    public void dispose() {
        ULog.out("dispose,path=" + path);
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

    void setModifyUrlCallback(ModifyUrlCallback mModifyUrlCallback) {
        this.mModifyUrlCallback = mModifyUrlCallback;
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
}
