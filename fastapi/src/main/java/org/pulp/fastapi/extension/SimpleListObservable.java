package org.pulp.fastapi.extension;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.pulp.fastapi.Bridge;
import org.pulp.fastapi.i.CachePolicy;
import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IListModel;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.CacheControl;
import retrofit2.Retrofit;

/**
 * 为SimpleObservable提供分页功能
 * Created by xinjun on 2019/12/4 16:38
 */
public class SimpleListObservable<T extends IListModel> extends SimpleObservable<T> {

    SimpleListObservable(Observable<T> upstream, Type observableType, Annotation[] annotations, Retrofit retrofit, Class<?> apiClass) {
        super(upstream, observableType, annotations, retrofit, apiClass);
        super.setSuccess(mSuccess);
    }


    private PageCondition<T> mPageCondition;
    private Map<Integer, T> allDatas = new HashMap<>();
    private Success<T> realSuccess;
    private Success<T> mSuccess = new Success<T>() {
        @Override
        public void onSuccess(@NonNull T data) {
            allDatas.put(data.onGetPageIndex(), data);
            if (realSuccess != null)
                realSuccess.onSuccess(data);
        }
    };

    @Override
    public SimpleListObservable<T> refresh() {
        setExtraParam(null);
        setCurrData(null);
        allDatas.clear();
        page(0);
        return (SimpleListObservable<T>) super.refresh();
    }

    /**
     * 前一页
     */
    public SimpleObservable<T> prePage() {
        if (mPageCondition == null) {
            throw new RuntimeException("not found page condition declare");
        }
        runInBox(new Runnable() {
            @Override
            public void run() {
                T dataForPre = getDataForPre();
                if (dataForPre == null) {
                    Log.out("prePage faild!because no data");
                    abortOnce();
                    return;
                }
                if (!mPageCondition.hasMore(dataForPre, PageCondition.MoreType.PrePage)) {
                    abortOnce();
                    final Error error = new Error();
                    error.setCode(Error.Code.NO_PREVIOUS_DATA.code);
                    error.setMsg("no previous data");
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Faild faildCallBack = getFaildCallBack();
                            if (faildCallBack != null)
                                faildCallBack.onFaild(error);
                            toastErrorIfNeed(error);
                        }
                    });
                    return;
                }
                setExtraParam(mPageCondition.prePage(dataForPre));
                subscribeIfNeed();
            }
        });
        return this;
    }

    /**
     * 后一页
     */
    @SuppressWarnings("UnusedReturnValue")
    public SimpleObservable<T> nextPage() {
        if (mPageCondition == null) {
            throw new RuntimeException("not found page condition declare");
        }
        runInBox(new Runnable() {
            @Override
            public void run() {
                T dataForNext = getDataForNext();
                if (dataForNext == null) {
                    Log.out("nextPage faild!because no data");
                    abortOnce();
                    return;
                }
                if (!mPageCondition.hasMore(dataForNext, PageCondition.MoreType.NextPage)) {
                    abortOnce();
                    final Error error = new Error();
                    error.setCode(Error.Code.NO_MORE_DATA.code);
                    error.setMsg("no more data");
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Faild faildCallBack = getFaildCallBack();
                            if (faildCallBack != null)
                                faildCallBack.onFaild(error);
                            toastErrorIfNeed(error);
                        }
                    });
                    return;
                }
                setExtraParam(mPageCondition.nextPage(dataForNext));
                subscribeIfNeed();
            }
        });
        return this;
    }

    /**
     * 某一页
     */
    public SimpleObservable<T> page(final int page) {
        if (mPageCondition == null) {
            throw new RuntimeException("not found page condition declare");
        }
        runInBox(new Runnable() {
            @Override
            public void run() {
                T currData = getCurrData();
                if (currData == null) {
                    Log.out("page faild!because no data");
                    abortOnce();
                    return;
                }
                Map<String, String> param = mPageCondition.page(currData, page);
                if (param == null) {
                    abortOnce();
                    final Error error = new Error();
                    error.setCode(Error.Code.NO_PAGE_DATA.code);
                    error.setMsg("no page data");
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Faild faildCallBack = getFaildCallBack();
                            if (faildCallBack != null)
                                faildCallBack.onFaild(error);
                            toastErrorIfNeed(error);
                        }
                    });
                    return;
                }
                setExtraParam(mPageCondition.page(currData, page));
                subscribeIfNeed();
            }
        });
        return this;
    }

    //catch class cast exception
    private void runInBox(Runnable run) {
        try {
            if (run != null)
                run.run();
        } catch (final ClassCastException e) {
            abortOnce();
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    final Error error = new Error();
                    error.setCode(Error.Code.PAGE_CONDITION_TYPE_BAD.code);
                    error.setMsg("check your page condition(" + mPageCondition.getClass().getName() + "),because " + e.getMessage());
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Faild faildCallBack = getFaildCallBack();
                            if (faildCallBack != null)
                                faildCallBack.onFaild(error);
                            toastErrorIfNeed(error);
                        }
                    });
                }
            });
        }
    }


    public SimpleListObservable<T> reset() {
        setCurrData(null);
        allDatas.clear();
        setExtraParam(null);
        return this;
    }

    @Override
    public SimpleListObservable<T> cachePolicy(CacheControl cacheControl) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.cachePolicy(cacheControl);
    }

    @Override
    public SimpleListObservable<T> cachePolicy(String cacheControlStr) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.cachePolicy(cacheControlStr);
    }

    @Override
    public SimpleListObservable<T> cachePolicy(CachePolicy cachePolicy) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.cachePolicy(cachePolicy);
    }

    @Override
    public SimpleListObservable<T> success(Success<T> success) {
        if (isAbort())
            return this;
        realSuccess = success;
        return (SimpleListObservable<T>) super.success(mSuccess);
    }

    @Override
    public SimpleListObservable<T> faild(Faild faild) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.faild(faild);
    }

    public SimpleListObservable<T> over(Over over) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.over(over);
    }

    public SimpleListObservable<T> setSuccess(Success<T> success) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.setSuccess(success);
    }


    public SimpleListObservable<T> setFaild(Faild faild) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.setFaild(faild);
    }

    public SimpleListObservable<T> setOver(Over over) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.setOver(over);
    }


    @Override
    public SimpleListObservable<T> toastError() {
        if (isAbort())
            return (SimpleListObservable<T>) super.toastErrorNoSubscribe();
        return (SimpleListObservable<T>) super.toastError();
    }

    public SimpleObservable<T> lookTimeUsed(@NonNull String tag) {
        if (isAbort())
            return this;
        return (SimpleListObservable<T>) super.lookTimeUsed(tag);
    }

    /**
     * 设置自定义的分页Condition
     *
     * @param mPageCondition PageCondition
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public SimpleObservable<T> pageCondition(PageCondition<T> mPageCondition) {
        this.mPageCondition = mPageCondition;
        return this;
    }


    private T getDataForPre() {
        Iterator<Integer> iterator = allDatas.keySet().iterator();
        int minKey = Integer.MAX_VALUE;
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            minKey = next < minKey ? next : minKey;
        }
        Log.out("getDataForPre.minKey=" + minKey);
        return allDatas.get(minKey);
    }

    private T getDataForNext() {
        Iterator<Integer> iterator = allDatas.keySet().iterator();
        int maxKey = Integer.MIN_VALUE;
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            maxKey = next > maxKey ? next : maxKey;
        }
        Log.out("getDataForNext.maxKey=" + maxKey);
        return allDatas.get(maxKey);
    }


}
