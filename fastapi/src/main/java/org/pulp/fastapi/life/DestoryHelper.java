package org.pulp.fastapi.life;


import org.pulp.fastapi.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * DestoryHelper配合DestoryListener解决rxjava,okhttp造成的泄漏问题
 * Created by xinjun on 2019/12/12 15:55
 */
public class DestoryHelper {

    private static Map<Object, Set<DestoryWatcher.DestoryListener>> listeners = new WeakHashMap<>();

    static void add(Object fromObj, DestoryWatcher.DestoryListener listener) {
        if (fromObj == null)
            return;
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj);
        if (destoryListeners == null) {
            destoryListeners = new HashSet<>();
            listeners.put(fromObj, destoryListeners);
        }
        listeners.get(fromObj).add(listener);
        Log.out("add.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
    }

    static void notify(Object fromObj) {
        if (fromObj == null)
            return;
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj);
        if (destoryListeners == null || destoryListeners.size() == 0)
            return;
        for (DestoryWatcher.DestoryListener li : destoryListeners)
            li.onDestory();
        destoryListeners.clear();
        listeners.remove(fromObj);
        Log.out("notify.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
    }

}
