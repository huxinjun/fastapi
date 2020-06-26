package org.pulp.fastapi.life;


public class Bridge {

    public static void addDestoryListener(Object fromObj, DestoryWatcher.DestoryListener listener) {
        DestoryHelper.add(fromObj, listener);
    }

}
