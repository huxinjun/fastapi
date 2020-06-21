package org.pulp.fastapi.life;


import org.pulp.fastapi.util.ULog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * DestoryHelper配合DestoryWatcher,DestoryListener解决rxjava,okhttp造成的泄漏问题
 * 使用方法:
 * 1.将要发起api请求的类实现DestoryWatcher接口,这个接口是一个标记接口,只有实现此接口才可使用ApiClient
 * 2.在实现了DestoryWatcher接口的Activity或者Fragment的onDestory调用DestoryHelper.notify(this);!!!!!!重要!!!!!!!!
 * Created by xinjun on 2019/12/12 15:55
 */
public class DestoryHelper {

    private static Map<Integer, Set<DestoryWatcher.DestoryListener>> listeners = new WeakHashMap<>();

    public static void add(DestoryWatcher fromObj, DestoryWatcher.DestoryListener listener) {
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj.hashCode());
        if (destoryListeners == null) {
            destoryListeners = new HashSet<>();
            listeners.put(fromObj.hashCode(), destoryListeners);
        }
        listeners.get(fromObj.hashCode()).add(listener);
        ULog.out("set.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
    }

    public static void notify(DestoryWatcher fromObj) {
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj.hashCode());
        if (destoryListeners == null || destoryListeners.size() == 0)
            return;
        ULog.out("notify.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
        for (DestoryWatcher.DestoryListener li : destoryListeners)
            li.onDestory();
        destoryListeners.clear();
        listeners.remove(fromObj.hashCode());
    }

}
