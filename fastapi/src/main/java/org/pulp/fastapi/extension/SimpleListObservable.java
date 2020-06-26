package org.pulp.fastapi.extension;


import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

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
        setExtraParam(mPageCondition.prePage(getCurrData()));
        subscribeIfNeed();
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
        if (!mPageCondition.hasMore(getCurrData())) {
            abortOnce();
            getHandler().post(() -> {
                Error error = new Error();
                error.setCode(Error.ERR_NO_MORE_DATA);
                error.setMsg("no more data");
                Faild faildCallBack = getFaildCallBack();
                if (faildCallBack != null)
                    faildCallBack.onFaild(error);
            });
            return this;
        }
        setExtraParam(mPageCondition.nextPage(getCurrData()));
        subscribeIfNeed();
        return this;
    }

    /**
     * 某一页并订阅
     */
    public SimpleObservable<T> page(int page) {
        setPage(page);
        subscribeIfNeed();
        return this;
    }

    /**
     * 某一页
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public SimpleObservable<T> setPage(int page) {
        if (mPageCondition == null) {
            throw new RuntimeException("not found page condition declare");
        }
        setExtraParam(mPageCondition.page(getCurrData(), page));
        return this;
    }

    public SimpleListObservable<T> reset() {
        setExtraParam(null);
        setCurrData(null);
        page(0);
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
}
