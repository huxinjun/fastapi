package org.pulp.fastapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.PathConverter;

import java.util.Map;

import okhttp3.logging.HttpLoggingInterceptor;

public interface Setting {


    @NonNull
    Context onGetApplicationContext();

    @NonNull
    String onGetCacheDir();

    long onGetCacheSize();

    @NonNull
    String onGetBaseUrl();


    @Nullable
    PathConverter onGetPathConverter();

    @Nullable
    <T> InterpreterParserCustom<T> onCustomParse(Class<T> dataClass);

    @Nullable
    InterpreterParseBefore onBeforeParse();

    @Nullable
    InterpreterParseError onErrorParse();

    @Nullable
    InterpreterParserAfter onAfterParse();

    @Nullable
    Map<String, String> onGetCommonParams();


    @Nullable
    HttpLoggingInterceptor.Logger onCustomLogger();

    @Nullable
    HttpLoggingInterceptor.Level onCustomLoggerLevel();

    /**
     * unit millisecond
     *
     * @return ConnectTimeout
     */
    int onGetConnectTimeout();

    /**
     * unit millisecond
     *
     * @return ReadTimeout
     */
    int onGetReadTimeout();
}
