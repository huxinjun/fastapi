package org.pulp.fastapi.factory;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.page.IModel;
import org.pulp.fastapi.page._String;
import org.pulp.fastapi.util.ULog;
import org.pulp.fastapi.extension.Error;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 数据实体转换
 * Created by xinjun on 2019/11/30 11:20
 */
public class AichangConverterFactory extends Converter.Factory {

    private static final String TAG = AichangConverterFactory.class.getSimpleName();
    public static final String TAG_CACHE = "@#!!!CACHE_TAG_NO_REPEAT!!!#@";


    /**
     * 回应数据描述
     * 为了支持实体cache标记
     */
    private class ResponseInfo {
        boolean isCache;
        String json;

        ResponseInfo() {
        }

        ResponseInfo(boolean isCache, String json) {
            this.isCache = isCache;
            this.json = json;
        }
    }


    private GsonBuilder gsonBuilder = new GsonBuilder();
    private GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

    private UrlKeyConverter urlkeyConverter = new UrlKeyConverter();//urlkey单独解析
    private StringModelConverter stringModelConverter = new StringModelConverter();//由于SimpleObservable限制了实体类型,使用此对象支持String

    public static AichangConverterFactory create() {
        return new AichangConverterFactory();
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        ULog.out("responseBodyConverter:type=" + type);
        if (type == UrlKey.class) {
            return urlkeyConverter;
        } else if (type == _String.class) {
            return stringModelConverter;
        } else {
            return new JsonConverter<>(type);
        }
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return gsonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }


    /**
     * 获取请求回来的json,这个json有可能被加入的cache标记,需要清除掉
     */
    @NonNull
    private ResponseInfo getResponseContent(ResponseBody value) throws IOException {
        String string = value.string();
        if (TextUtils.isEmpty(string))
            return new ResponseInfo(false, string);
        ResponseInfo info = new ResponseInfo();
        info.isCache = string.startsWith(AichangConverterFactory.TAG_CACHE);
        info.json = string.replace(AichangConverterFactory.TAG_CACHE, "");
        return info;
    }

    /**
     * 可直接返回请求结果的字符序列
     * Created by xinjun on 2019/12/4 16:25
     */
    private class StringModelConverter implements Converter<ResponseBody, _String> {
        @Override
        public _String convert(@NonNull ResponseBody value) throws IOException {
            return new _String(getResponseContent(value).json);
        }
    }

    /**
     * UrlKey自定义转换,需要转换为特定格式
     * Created by xinjun on 2019/11/30 14:49
     */
    private class UrlKeyConverter implements Converter<ResponseBody, UrlKey> {

        @Override
        public UrlKey convert(@NonNull ResponseBody value) throws IOException {
            return parseUrlKey(getResponseContent(value).json);
        }
    }

    /**
     * gson转换
     * Created by xinjun on 2019/11/30 14:49
     */
    private class JsonConverter<T extends IModel> implements Converter<ResponseBody, T> {

        Type type;

        JsonConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(@NonNull ResponseBody value) throws IOException {
            ResponseInfo responseInfo = getResponseContent(value);
            String jsonStr = responseInfo.json;

            try {
                //全局错误处理
                JSONObject jsonObject = new JSONObject(jsonStr);
                Error error = parseError(jsonObject);
                if (error != null) {
                    String err2str = Error.Companion.err2str(error);
                    throw new RuntimeException(err2str);
                }

                //result提取剥离
                if (jsonObject.has("result")) {
                    Object result = jsonObject.opt("result");
                    if (jsonObject.length() == 1 && result instanceof JSONObject)
                        jsonStr = result.toString();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Gson gson = gsonBuilder.create();
            T data = null;
            try {
                data = gson.fromJson(jsonStr, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ULog.out("parse:data=" + data);
            if (data == null)
                throw new RuntimeException("parse bean error,please check your bean");

            boolean cacheResponse = responseInfo.isCache;
            ULog.out("parse:is cache response=" + cacheResponse);
            data.onSetIsCache(cacheResponse);

            return data;
        }
    }


    private Error parseError(JSONObject obj) {
        if (obj != null) {
            if (obj.has("error") && obj.has("code")) {
                Error error = new Error();
                error.setCode(obj.optInt("code", ContextError.NoError));
                error.setStatus(obj.optString("status", ""));
                error.setMsg(obj.optString("errmsg", ""));
                if (TextUtils.isEmpty(error.getMsg()))
                    error.setMsg(obj.optString("result"));
                String desc = ContextError.errorDict.get(error.getCode());
                error.setDesc(desc == null ? "未知错误" : desc);
                return error;
            }
        }
        return null;
    }

}
