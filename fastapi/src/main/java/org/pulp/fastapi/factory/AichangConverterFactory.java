package org.pulp.fastapi.factory;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.Get;
import org.pulp.fastapi.i.Parser;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.model._String;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.ULog;
import org.pulp.fastapi.model.Error;

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
    public static final String TAG_EXTRA = "@#!!!EXTRA_TAG_NO_REPEAT!!!#@";


    /**
     * 回应数据描述
     * 为了支持实体cache标记
     */
    private class ResponseInfo {
        boolean isCache;
        String json;
        String dataParserClass;

        ResponseInfo() {
        }

        ResponseInfo(boolean isCache, String json) {
            this.isCache = isCache;
            this.json = json;
        }

        @NotNull
        @Override
        public String toString() {
            return "ResponseInfo{" +
                    "isCache=" + isCache +
                    ", json='" + json + '\'' +
                    ", dataParserClass='" + dataParserClass + '\'' +
                    '}';
        }
    }


    private GsonBuilder gsonBuilder = new GsonBuilder();
    private GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

    private StringModelConverter stringModelConverter = new StringModelConverter();//由于SimpleObservable限制了实体类型,使用此对象支持String

    public static AichangConverterFactory create() {
        return new AichangConverterFactory();
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        ULog.out("responseBodyConverter:type=" + type);
        if (type == _String.class) {
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
        String[] split = string.split(TAG_EXTRA);
        if (split.length == 1)
            return new ResponseInfo(false, string);
        for (int i = 0; i < split.length - 1; i++) {
            String extra = split[i];
            String[] param = extra.split("=");
            if (param.length == 2) {
                String k = param[0].trim();
                String v = param[1].trim();
                switch (k) {
                    case "Cache":
                        info.isCache = Boolean.parseBoolean(v);
                        break;
                    case "DataParser":
                        info.dataParserClass = v;
                        break;
                }
            }
        }
        info.json = split[split.length - 1];

        ULog.out("getResponseContent:" + info);
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

            if (TextUtils.isEmpty(jsonStr))
                return null;

            Parser mParser = Get.getParser();
            String dataParserClass = responseInfo.dataParserClass;
            try {
                if (!TextUtils.isEmpty(dataParserClass))
                    mParser = (Parser) Class.forName(dataParserClass).newInstance();
            } catch (Exception e) {
                CommonUtil.throwError(Error.ERR_PARSE_CLASS, "can't instantiated custom Parser:" + dataParserClass);
            }

            if (mParser != null) {
                try {

                    Error error = mParser.onParseError(jsonStr);
                    if (error != null)
                        CommonUtil.throwError(error);
                    String afterJsonStr = mParser.onBeforeParse(jsonStr);
                    if (!TextUtils.isEmpty(afterJsonStr))
                        jsonStr = afterJsonStr;

                    //user parse data
                    @SuppressWarnings("unchecked")
                    T parse = (T) mParser.onCustomParse(jsonStr);
                    if (parse != null) {
                        parse.setCache(responseInfo.isCache);
                        return parse;
                    }
                } catch (Exception e) {
                    CommonUtil.throwError(Error.ERR_PARSE_CUSTOM, "custom parse error:msg is" + e.getMessage()
                            + ",parse class is" + responseInfo.dataParserClass);
                }
            }

            //frame work parse data
            Gson gson = gsonBuilder.create();
            T data = null;
            try {
                data = gson.fromJson(jsonStr, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ULog.out("parse:data=" + data);
            if (data == null) {
                CommonUtil.throwError(Error.ERR_PARSE_BEAN, "parse error");
            }

            boolean cacheResponse = responseInfo.isCache;
            ULog.out("parse:is cache response=" + cacheResponse);
            if (data != null) {
                data.setCache(cacheResponse);
            }
            return data;
        }
    }


}
