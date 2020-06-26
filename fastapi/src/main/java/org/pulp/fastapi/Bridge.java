package org.pulp.fastapi;

import android.content.Context;

import okhttp3.Cache;

public class Bridge {

    private static API getClient() {
        return API.getClient();
    }

    public static Context getContext() {
        return getClient().getApplicationContext();
    }

    public static Setting getSetting() {
        return getClient().getSetting();
    }

    public static Cache getCache() {
        return getClient().getCache();
    }


}
