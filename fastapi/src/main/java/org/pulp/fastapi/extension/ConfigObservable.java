package org.pulp.fastapi.extension;

import android.text.TextUtils;


import org.pulp.fastapi.page.IModel;
import org.pulp.fastapi.util.ULog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;

/**
 * ConfigObservable支持configurl切换
 * Created by xinjun on 2019/12/12 21:47
 */
public class ConfigObservable<T extends IModel> extends SimpleObservable<T> {

    /**
     * 配置的url无法访问回调
     * Created by xinjun on 2019/12/13 10:35
     */
    public interface Unreachable {
        void onUnreachable(ConfigObservable parent, Error error);
    }

    private String[] urls;
    private int currIndex = 0;
    private Unreachable unreachableCallback;//url无法访问回调

    ConfigObservable(Observable<T> upstream, Type observableType, Annotation[] annotations) {
        super(upstream, observableType, annotations);
    }

    @Override
    public ConfigObservable<T> refresh() {
        setExtraParam(null);
        setCurrData(null);
        return (ConfigObservable<T>) super.refresh();
    }

    @Override
    public ConfigObservable<T> success(Success<T> success) {
        return (ConfigObservable<T>) super.success(data -> {
            success.onSuccess(data);
            dispose();
        });
    }

    @Override
    public ConfigObservable<T> faild(Faild faild) {
        return (ConfigObservable<T>) super.faild(faild);
    }

    @Override
    public ConfigObservable<T> toastError() {
        return (ConfigObservable<T>) super.toastError();
    }


    public ConfigObservable<T> unreachable(Unreachable unreachable) {
        this.unreachableCallback = unreachable;
        return (ConfigObservable<T>) super.faild(error -> {
            ULog.out("unreachable.error:" + error);
            if (unreachableCallback != null)
                unreachableCallback.onUnreachable(ConfigObservable.this, error);
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
