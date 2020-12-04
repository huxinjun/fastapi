package org.pulp.fastapi.extension;

import android.text.TextUtils;


import androidx.annotation.NonNull;

import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import retrofit2.Retrofit;

/**
 * 单接口多url顺序请求,直到成功为止
 * Created by xinjun on 2019/12/12 21:47
 */
public class SequenceObservable<T extends IModel> extends SimpleObservable<T> {

    /**
     * 配置的path无法访问回调
     * Created by xinjun on 2019/12/13 10:35
     */
    public interface Unreachable {
        void onUnreachable(Error error, String path);
    }

    private String[] paths;
    private int currIndex = 0;
    private Unreachable unreachableCallback;//url无法访问回调
    private Faild faild;

    SequenceObservable(Observable<T> upstream, Type observableType, Annotation[] annotations, Retrofit retrofit, Class<?> apiClass) {
        super(upstream, observableType, annotations, retrofit, apiClass);
    }

    @Override
    public SequenceObservable<T> refresh() {
        setExtraParam(null);
        setCurrData(null);
        super.success(new Success<T>() {
            @Override
            public void onSuccess(@NonNull T data) {
                dispose();
            }
        });
        super.faild(new Faild() {
            @Override
            public void onFaild(@NonNull Error error) {
                Log.out("success.faild hash=" + this.hashCode());
                if (unreachableCallback != null)
                    unreachableCallback.onUnreachable(error, getCurrPath());
                nextUrl();
            }
        });
        return this;
    }

    @Override
    public SequenceObservable<T> success(final Success<T> success) {
        super.success(new Success<T>() {
            @Override
            public void onSuccess(@NonNull T data) {
                if (success != null)
                    success.onSuccess(data);
                dispose();
            }
        });
        super.faild(new Faild() {
            @Override
            public void onFaild(@NonNull Error error) {
                Log.out("success.faild hash=" + this.hashCode());
                if (unreachableCallback != null)
                    unreachableCallback.onUnreachable(error, getCurrPath());
                nextUrl();
            }
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
        if (currIndex >= paths.length - 1) {
            Error error = new Error();
            error.setCode(Error.Code.ALL_URLS_INVALID.code);
            error.setMsg("all path is unreachable");
            if (this.faild != null)
                this.faild.onFaild(error);
            toastErrorIfNeed(error);
            dispose();
            return;
        }
        currIndex++;
        refresh();
    }


    String getCurrPath() {
        if (paths == null || paths.length == 0)
            throw new RuntimeException("not found paths,please use @MultiPath annotation above api method");
        String currPath = paths[currIndex];
        if (TextUtils.isEmpty(currPath))
            throw new RuntimeException("@MultiPath value item must not be null or emtpy");
        return currPath;
    }


    void setPaths(String[] paths) {
        this.paths = paths;
    }
}
