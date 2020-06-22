package org.pulp.fastapi.extension;

import android.text.TextUtils;


import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
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
        void onUnreachable(SequenceObservable parent, Error error);
    }

    private String[] urls;
    private int currIndex = 0;
    private Unreachable unreachableCallback;//url无法访问回调

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
        return (SequenceObservable<T>) super.success(data -> {
            success.onSuccess(data);
            dispose();
        });
    }

    @Override
    public SequenceObservable<T> faild(Faild faild) {
        return (SequenceObservable<T>) super.faild(faild);
    }

    @Override
    public SequenceObservable<T> toastError() {
        return (SequenceObservable<T>) super.toastError();
    }


    public SequenceObservable<T> unreachable(Unreachable unreachable) {
        this.unreachableCallback = unreachable;
        return (SequenceObservable<T>) super.faild(error -> {
            ULog.out("unreachable.error:" + error);
            if (unreachableCallback != null)
                unreachableCallback.onUnreachable(SequenceObservable.this, error);
        });
    }


    public void nextUrl() {
        if (currIndex >= urls.length - 1) {
            ULog.out("all config url is unreachable!!!");
            dispose();
            return;
        }
        currIndex++;
        refresh();
    }


    String getCurrUrl() {
        if (urls == null || urls.length == 0)
            throw new RuntimeException("no config url,please use @CONFIG annotation above config api method");
        String currUrl = urls[currIndex];
        if (TextUtils.isEmpty(currUrl))
            throw new RuntimeException("config url must not be null or emtpy");
        return currUrl;
    }


    void setUrls(String[] urls) {
        this.urls = urls;
    }
}
