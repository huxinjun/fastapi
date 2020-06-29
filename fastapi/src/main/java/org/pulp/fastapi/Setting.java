package org.pulp.fastapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.PageCondition;
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

    @Nullable
    PageCondition onGetPageCondition();

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

    /**
     * framework error msg replace to custom
     * used in SimpleListObservable:
     * Error.ERR_NO_MORE_DATA
     * Error.ERR_NO_PREVIOUS_DATA
     * Error.ERR_NO_PAGE_DATA
     */
    @Nullable
    String onErrorCode2String(int code);
}
