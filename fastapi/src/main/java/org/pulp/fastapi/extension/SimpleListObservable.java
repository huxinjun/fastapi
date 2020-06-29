package org.pulp.fastapi.extension;


import android.text.TextUtils;

import org.pulp.fastapi.Bridge;
import org.pulp.fastapi.i.CachePolicy;
import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.CacheControl;
import retrofit2.Retrofit;

/**
 * 为SimpleObservable提供分页功能
 * Created by xinjun on 2019/12/4 16:38
 */
public class SimpleListObservable<T extends IModel> extends SimpleObservable<T> {

    SimpleListObservable(Observable<T> upstream, Type observableType, Annotation[] annotations, Retrofit retrofit, Class<?> apiClass) {
        super(upstream, observableType, annotations, retrofit, apiClass);
    }


    private PageCondition<T> mPageCondition;

    @Override
    public SimpleListObservable<T> refresh() {
        setExtraParam(null);
        setCurrData(null);
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
                Map<String, String> param = mPageCondition.prePage(getCurrData());
                if (param == null) {
                    abortOnce();
                    Error error = new Error();
                    error.setCode(Error.ERR_NO_PREVIOUS_DATA);
                    error.setMsg(generateErrorMsg(error.getCode()));
                    Faild faildCallBack = getFaildCallBack();
                    if (faildCallBack != null)
                        faildCallBack.onFaild(error);
                    return;
                }
                setExtraParam(param);
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
                if (!mPageCondition.hasMore(getCurrData())) {
                    abortOnce();
                    Error error = new Error();
                    error.setCode(Error.ERR_NO_MORE_DATA);
                    error.setMsg(generateErrorMsg(error.getCode()));
                    Faild faildCallBack = getFaildCallBack();
                    if (faildCallBack != null)
                        faildCallBack.onFaild(error);
                    return;
                }
                setExtraParam(mPageCondition.nextPage(getCurrData()));
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
                Map<String, String> param = mPageCondition.page(getCurrData(), page);
                if (param == null) {
                    abortOnce();
                    Error error = new Error();
                    error.setCode(Error.ERR_NO_PAGE_DATA);
                    error.setMsg(generateErrorMsg(error.getCode()));
                    Faild faildCallBack = getFaildCallBack();
                    if (faildCallBack != null)
                        faildCallBack.onFaild(error);
                    return;
                }
                setExtraParam(mPageCondition.page(getCurrData(), page));
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
                    Error error = new Error();
                    error.setCode(Error.ERR_PAGE_CONDITION_TYPE_BAD);
                    error.setMsg("check your page condition(" + mPageCondition.getClass().getName() + "),because " + e.getMessage());
                    Faild faildCallBack = getFaildCallBack();
                    if (faildCallBack != null)
                        faildCallBack.onFaild(error);
                }
            });
        }
    }


    public SimpleListObservable<T> reset() {
        setCurrData(null);
        setExtraParam(mPageCondition.nextPage(null));
        return this;
    }

    @Override
    public SimpleListObservable<T> cachePolicy(CacheControl cacheControl) {
        return (SimpleListObservable<T>) super.cachePolicy(cacheControl);
    }

    @Override
    public SimpleListObservable<T> cachePolicy(String cacheControlStr) {
        return (SimpleListObservable<T>) super.cachePolicy(cacheControlStr);
    }

    @Override
    public SimpleListObservable<T> cachePolicy(CachePolicy cachePolicy) {
        return (SimpleListObservable<T>) super.cachePolicy(cachePolicy);
    }

    @Override
    public SimpleListObservable<T> success(Success<T> success) {
        return (SimpleListObservable<T>) super.success(success);
    }

    @Override
    public SimpleListObservable<T> faild(Faild faild) {
        return (SimpleListObservable<T>) super.faild(faild);
    }

    public SimpleListObservable<T> over(Over over) {
        return (SimpleListObservable<T>) super.over(over);
    }

    public SimpleListObservable<T> setSuccess(Success<T> success) {
        return (SimpleListObservable<T>) super.setSuccess(success);
    }


    public SimpleListObservable<T> setFaild(Faild faild) {
        return (SimpleListObservable<T>) super.setFaild(faild);
    }

    public SimpleListObservable<T> setOver(Over over) {
        return (SimpleListObservable<T>) super.setOver(over);
    }


    @Override
    public SimpleListObservable<T> toastError() {
        return (SimpleListObservable<T>) super.toastError();
    }

    /**
     * 设置自定义的分页Condition
     *
     * @param mPageCondition PageCondition
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public SimpleObservable<T> pageCondition(PageCondition<T> mPageCondition) {
        boolean isFirstSet = this.mPageCondition == null;
        this.mPageCondition = mPageCondition;
        if (isFirstSet)
            setExtraParam(mPageCondition.nextPage(getCurrData()));
        return this;
    }


    private String generateErrorMsg(int code) {
        String codeToString = Bridge.getSetting().onErrorCode2String(code);
        if (!TextUtils.isEmpty(codeToString))
            return codeToString;
        switch (code) {
            case Error.ERR_NO_PAGE_DATA:
                return "no page data";
            case Error.ERR_NO_PREVIOUS_DATA:
                return "no previous data";
            case Error.ERR_NO_MORE_DATA:
                return "no more data";
        }
        return null;
    }
}
