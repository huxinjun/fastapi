package org.pulp.fastapi;

import android.content.Context;

import org.pulp.fastapi.i.PathConverter;

import java.util.Map;

import okhttp3.Cache;
import retrofit2.Retrofit;

public class Bridge {

    private static ApiClient getClient() {
        return ApiClient.getClient();
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
