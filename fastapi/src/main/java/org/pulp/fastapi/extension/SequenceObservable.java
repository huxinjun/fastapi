package org.pulp.fastapi.extension;

import android.text.TextUtils;


import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.ULog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;

/**
 * 单接口多url顺序请求,直到成功为止
 * Created by xinjun on 2019/12/12 21:47
 */
public class SequenceObservable<T extends IModel> extends SimpleObservable<T> {

    /**
     * 配置的url无法访问回调
     * Created by xinjun on 2019/12/13 10:35
     */
    public interface Unreachable {
        void onUnreachable(Error error, String url);
    }

    private String[] urls;
    private int currIndex = 0;
    private Unreachable unreachableCallback;//url无法访问回调
    private Faild faild;

    SequenceObservable(Observable<T> upstream, Type observableType, Annotation[] annotations) {
        super(upstream, observableType, annotations);
    }

    @Override
    public SequenceObservable<T> refresh() {
        setExtraParam(null);
        setCurrData(null);
        return (SequenceObservable<T>) super.refresh();
    }

    @Override
    public SequenceObservable<T> success(Success<T> success) {
        super.success(data -> {
            success.onSuccess(data);
            dispose();
        });
        super.faild(error -> {
            ULog.out("success.faild hash=" + this.hashCode());
            if (unreachableCallback != null)
                unreachableCallback.onUnreachable(error, getCurrUrl());
            nextUrl();
        });
        return this;
    }

    @Override
    public SequenceObservable<T> faild(Faild faild) {
        this.faild = faild;
        return this;
    }

    @Override
    public SequenceObservable<T> toastError() {
        return (SequenceObservable<T>) super.toastError();
    }


    public SequenceObservable<T> unreachable(Unreachable unreachable) {
        this.unreachableCallback = unreachable;
        return this;
    }


    private void nextUrl() {
        if (currIndex >= urls.length - 1) {
            Error error = new Error();
            error.setCode(Error.ERR_ALL_URLS_INVALID);
            error.setMsg("all url is unreachable");
            this.faild.onFaild(error);
            dispose();
            return;
        }
        currIndex++;
        refresh();
    }


    String getCurrUrl() {
        if (urls == null || urls.length == 0)
            throw new RuntimeException("not found urls,please use @MultiPath annotation above api method");
        String currUrl = urls[currIndex];
        if (TextUtils.isEmpty(currUrl))
            throw new RuntimeException("@MultiPath value item must not be null or emtpy");
        return currUrl;
    }


    void setUrls(String[] urls) {
        this.urls = urls;
    }
}
