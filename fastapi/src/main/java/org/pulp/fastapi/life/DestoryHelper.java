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

    private static Map<Integer, Set<DestoryWatcher.DestoryListener>> listeners = new WeakHashMap<>();

    static void add(Object fromObj, DestoryWatcher.DestoryListener listener) {
        if (fromObj == null)
            return;
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj.hashCode());
        if (destoryListeners == null) {
            destoryListeners = new HashSet<>();
            listeners.put(fromObj.hashCode(), destoryListeners);
        }
        listeners.get(fromObj.hashCode()).add(listener);
        Log.out("set.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
    }

    static void notify(Object fromObj) {
        if (fromObj == null)
            return;
        Set<DestoryWatcher.DestoryListener> destoryListeners = listeners.get(fromObj.hashCode());
        if (destoryListeners == null || destoryListeners.size() == 0)
            return;
        Log.out("notify.watcher_size=" + listeners.size() + ",listener_size=" + destoryListeners.size() + ",fromObj=" + fromObj);
        for (DestoryWatcher.DestoryListener li : destoryListeners)
            li.onDestory();
        destoryListeners.clear();
        listeners.remove(fromObj.hashCode());
    }

}
