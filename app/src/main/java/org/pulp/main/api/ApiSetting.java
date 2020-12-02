package org.pulp.main.api;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.pulp.fastapi.Setting;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.i.PathConverter;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.util.Log;
import org.pulp.main.model.UrlKey;
import org.pulp.main.page.CommonPageCondition;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.logging.HttpLoggingInterceptor;

public class ApiSetting implements Setting {

    private Context context;
    private Map<String, String> commonParams = new HashMap<String, String>() {
        {
            put("base_market", "test");
        }
    };

    static ApiSetting get(Context context) {
        return new ApiSetting(context);
    }

    private ApiSetting(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Context onGetApplicationContext() {
        return context;
    }

    @NonNull
    @Override
    public String onGetCacheDir() {
        return new File(Environment.getExternalStorageDirectory().getPath(), "fastapi2").getPath();
    }

    @Override
    public long onGetCacheSize() {
        return 10 * 1024 * 1024;
    }

    @NonNull
    @Override
    public String onGetBaseUrl() {
        return "http://www.baidu.com";
    }

    @Nullable
    @Override
    public PathConverter onGetPathConverter() {
        return path -> {
            String url = null;
            if (UrlKey.downloadUrls != null)
                url = UrlKey.downloadUrls.get(path);
            if (TextUtils.isEmpty(url))
                url = UrlKey.defaultUrls.get(path);
            return url;
        };
    }

    @Nullable
    @Override
    public <T> InterpreterParserCustom<T> onCustomParse(Class<T> dataClass) {
//        Log.out("TestMethodParserAnno.onCustomParse");
//        if (dataClass == TestModel.class) {
//            //noinspection unchecked
//            return (InterpreterParserCustom<T>) (InterpreterParserCustom<TestModel>) json -> {
//
//                TestModel testModel = new TestModel();
//                testModel.testFrom = "global onCustomParse";
//                return testModel;
//            };
//        }
        return null;
    }

    @Nullable
    @Override
    public InterpreterParseBefore onBeforeParse() {
//        return json -> {
//            try {
//                JSONObject jsonObject = new JSONObject(json);
//                jsonObject.put("testFrom", "global onBeforeParse");
//                return jsonObject.toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        };
        return null;
    }

    @Nullable
    @Override
    public InterpreterParseError onErrorParse() {
        return null;
//        return new InterpreterParseError() {
//            @Override
//            public Error onParseError(String json) {
//                Log.out("TestClassParserAnno.onParseError");
//                Error error = new Error();
//                error.setCode(888);
//                error.setMsg("global onErrorParse");
//                return error;
//            }
//        };
    }

    @Nullable
    @Override
    public InterpreterParserAfter onAfterParse() {
        return bean -> Log.out("global onAfterParse:" + bean);
    }

    @Nullable
    @Override
    public Map<String, String> onGetCommonParams() {
        return commonParams;
    }

    @Nullable
    @Override
    public Map<String, String> onGetCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("header_test_key", "header_test_value");
        return headers;
    }

    @Nullable
    @Override
    public HttpLoggingInterceptor.Logger onCustomLogger() {
        return null;
    }

    @Nullable
    @Override
    public HttpLoggingInterceptor.Level onCustomLoggerLevel() {
        return null;
    }

    @Nullable
    @Override
    public PageCondition onGetPageCondition() {
        return new CommonPageCondition();
    }

    @Override
    public int onGetConnectTimeout() {
        return 1000;
    }

    @Override
    public int onGetReadTimeout() {
        return 1000;
    }

    @Override
    public void onToastError(@NonNull Error error) {

    }
}
