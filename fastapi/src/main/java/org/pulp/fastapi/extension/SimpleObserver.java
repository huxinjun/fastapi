package org.pulp.fastapi.extension;

import android.text.TextUtils;


import org.pulp.fastapi.model.Error;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 简单的observer
 * Created by xinjun on 2019/12/16 11:37
 */
public class SimpleObserver<T> implements Observer<T> {

    private SimpleObservable.Success<T> success;
    private SimpleObservable.Faild faild;

    @Override
    public void onSubscribe(Disposable disposable) {
        //ignore
    }

    @Override
    public void onNext(T t) {
        if (success != null && t != null)
            success.onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        String message = e.getMessage();
        Error error;
        if (!TextUtils.isEmpty(message) && message.startsWith(Error.Companion.getSYMBOL())) {
            error = Error.Companion.str2err(message);
        } else {
            error = new Error();
            error.setCode(Error.ERR_CRASH);
            error.setMsg("application error,open logcat to preview Warning log or stack detail:" + message);
        }
        if (faild != null)
            faild.onFaild(error);
    }

    @Override
    public void onComplete() {
        //ignore
    }


    public SimpleObserver<T> success(SimpleObservable.Success<T> success) {
        this.success = success;
        return this;
    }

    public SimpleObserver<T> faild(SimpleObservable.Faild faild) {
        this.faild = faild;
        return this;
    }
}
