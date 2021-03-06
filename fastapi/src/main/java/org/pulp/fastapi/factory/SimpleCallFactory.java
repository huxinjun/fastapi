package org.pulp.fastapi.factory;


import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import org.pulp.fastapi.util.Log;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 暂时不用,如果有全局通用的需求可以修改截获的Request
 * 也可以用来打自定义的日志
 * Created by xinjun on 2019/12/6 13:50
 */
public class SimpleCallFactory implements Call.Factory {

    private static SimpleCallFactory mInstance;

    private OkHttpClient okHttpClient;

    public static SimpleCallFactory getInstance(OkHttpClient okHttpClient) {
        if (mInstance == null)
            mInstance = new SimpleCallFactory(okHttpClient);
        return mInstance;
    }

    private SimpleCallFactory(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public Call newCall(@NonNull Request request) {
        Log.out("RequestWatcher.create request=" + Thread.currentThread().getId());
        //此处可截获请求前的Request
        RequestWatcher requestWatcher = requestWatcherMap.get(Thread.currentThread().getId());
        Request newRequest = request;
        if (requestWatcher != null) {
            newRequest = requestWatcher.onRequestCreated(request);
            requestWatcherMap.remove(Thread.currentThread().getId());
        }
        return okHttpClient.newCall(newRequest);
    }


    @SuppressLint("UseSparseArrays")
    private Map<Long, RequestWatcher> requestWatcherMap = new HashMap<>();

    public void setRequestWatcher(long threadId, RequestWatcher requestWatcher) {
        requestWatcherMap.put(threadId, requestWatcher);
        Log.out("RequestWatcher.setWatcher=" + threadId);
    }

    public interface RequestWatcher {
        Request onRequestCreated(Request request);
    }
}
