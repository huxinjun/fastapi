package org.pulp.fastapi.extension;


import androidx.annotation.Nullable;

import org.pulp.fastapi.page.DeffaultPageCondition;
import org.pulp.fastapi.page.IListModel;
import org.pulp.fastapi.util.ULog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.CacheControl;

/**
 * 为SimpleObservable提供分页功能
 * Created by xinjun on 2019/12/4 16:38
 */
public class SimpleListObservable<T extends IListModel> extends SimpleObservable<T> {

    /**
     * 自定义分页回调
     * Created by xinjun on 2019/12/9 11:13
     */
    public interface PageCondition<T> {
        /**
         * 下一页数据
         *
         * @param data 当前数据
         * @return 要添加在请求中的参数
         */
        Map<String, String> nextPage(@Nullable T data);

        /**
         * 上一页数据
         *
         * @param data 当前数据
         * @return 要添加在请求中的参数
         */
        Map<String, String> prePage(@Nullable T data);

        /**
         * 指定某一页数据
         */
        Map<String, String> page(@Nullable T data, int page);
    }

    SimpleListObservable(Observable<T> upstream, Type observableType, Annotation[] annotations) {
        super(upstream, observableType, annotations);
    }


    PageCondition<T> mPageCondition;

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
        if (mPageCondition != null)
            setExtraParam(mPageCondition.prePage(getCurrData()));
        else {
            mPageCondition = new DeffaultPageCondition<>();
            setExtraParam(mPageCondition.prePage(getCurrData()));
        }
        subscribeIfNeed();
        return this;
    }

    /**
     * 后一页
     */
    @SuppressWarnings("UnusedReturnValue")
    public SimpleObservable<T> nextPage() {
        if (!hasMore()) {
            abortOnce();
            getHandler().post(() -> {
                Error error = new Error();
                error.setCode(Error.ERR_NO_MORE_DATA);
                error.setStatus("no more data");
                error.setMsg("没有更多数据了");
                Faild faildCallBack = getFaildCallBack();
                if (faildCallBack != null)
                    faildCallBack.onFaild(error);
            });
            return this;
        }
        if (mPageCondition != null)
            setExtraParam(mPageCondition.nextPage(getCurrData()));
        else {
            mPageCondition = new DeffaultPageCondition<>();
            setExtraParam(mPageCondition.nextPage(getCurrData()));
        }
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
        if (mPageCondition != null)
            setExtraParam(mPageCondition.page(getCurrData(), page));
        else {
            mPageCondition = new DeffaultPageCondition<>();
            setExtraParam(mPageCondition.page(getCurrData(), page));
        }
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
     * 当前数据是否可以请求更多
     */
    private boolean hasMore() {
        boolean hasMore = getCurrData() == null || getCurrData().hasMore();
        ULog.out("hasMore=" + hasMore + ",class="
                + (getCurrData() == null ? "" : getCurrData().getClass()));
        return hasMore;
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
