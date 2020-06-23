package org.pulp.fastapi;

import android.content.Context;

import org.pulp.fastapi.i.Parser;
import org.pulp.fastapi.i.PathConverter;

import java.util.Map;

import okhttp3.Cache;
import retrofit2.Retrofit;

public class Get {

    public static ApiClient getClient() {
        return ApiClient.getClient();
    }

    public static Context getContext() {
        return getClient().getApplicationContext();
    }

    public static Cache getCache() {
        return getClient().getCache();
    }

    public static Retrofit getRetrofit() {
        return getClient().getRetrofit();
    }

    public static PathConverter getPathConverter() {
        return getClient().getPathConverter();
    }

    public static Parser getParser() {
        return getClient().getDataParser();
    }

    public static Map<String, String> getCommonParams() {
        return getClient().getCommonParams();
    }
}
