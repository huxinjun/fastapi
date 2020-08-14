package org.pulp.fastapi.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.pulp.fastapi.Bridge;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.model.Str;
import org.pulp.fastapi.util.ChainUtil;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.Log;
import org.pulp.fastapi.model.Error;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 数据实体转换
 * Created by xinjun on 2019/11/30 11:20
 */
public class SimpleConverterFactory extends Converter.Factory {

    private static final String TAG = SimpleConverterFactory.class.getSimpleName();
    public static final String TAG_EXTRA = "@#!!!EXTRA_TAG_NO_REPEAT!!!#@";


    /**
     * 回应数据描述
     * 为了支持实体cache标记
     */
    private class ResponseInfo {
        boolean isCache;
        String json;
        List<InterpreterParseBefore> beforeParser;
        List<InterpreterParseError> errorParser;
        List<InterpreterParserCustom> customParser;
        List<InterpreterParserAfter> afterParser;
        String timeLogFlag = null;
        long lastTime = 0;

        ResponseInfo() {
        }

        ResponseInfo(boolean isCache, String json) {
            this.isCache = isCache;
            this.json = json;
        }

        @Override
        public String toString() {
            return "ResponseInfo{" +
                    "isCache=" + isCache +
                    ", json='" + json + '\'' +
                    ", beforeParser=" + beforeParser +
                    ", errorParser=" + errorParser +
                    ", customParser=" + customParser +
                    ", afterParser=" + afterParser +
                    '}';
        }
    }


    private GsonBuilder gsonBuilder = new GsonBuilder();
    private GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

    private StringModelConverter stringModelConverter = new StringModelConverter();//由于SimpleObservable限制了实体类型,使用此对象支持String

    public static SimpleConverterFactory create() {
        return new SimpleConverterFactory();
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Log.out("responseBodyConverter:type=" + type);
        if (type == Str.class) {
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
    private ResponseInfo getResponseContent(ResponseBody value, Class dataClass) throws IOException {
        String string = value.string();
        if (TextUtils.isEmpty(string))
            return new ResponseInfo(false, string);
        ResponseInfo info = new ResponseInfo();
        String[] split = string.split(TAG_EXTRA);
        for (int i = 0; split.length > 1 && i < split.length - 1; i++) {
            String extra = split[i];
            String[] param = extra.split("=");
            if (param.length == 2) {
                String k = param[0].trim();
                String v = param[1].trim();
                try {
                    switch (k) {
                        case "Cache":
                            info.isCache = Boolean.parseBoolean(v);
                            break;
                        case InterpreterParseBefore.HEADER_FLAG:
                            info.beforeParser = new ArrayList<>();
                            String[] classNamesBefore = v.split(":");
                            for (String className : classNamesBefore) {
                                Object o = Class.forName(className).newInstance();
                                if (o instanceof InterpreterParseBefore)
                                    info.beforeParser.add((InterpreterParseBefore) o);
                            }

                            break;
                        case InterpreterParseError.HEADER_FLAG:
                            info.errorParser = new ArrayList<>();
                            String[] classNamesError = v.split(":");
                            for (String className : classNamesError) {
                                Object o = Class.forName(className).newInstance();
                                if (o instanceof InterpreterParseError)
                                    info.errorParser.add((InterpreterParseError) o);
                            }
                            break;
                        case InterpreterParserCustom.HEADER_FLAG:
                            info.customParser = new ArrayList<>();
                            String[] classNamesCustom = v.split(":");
                            for (String className : classNamesCustom) {
                                Object o = Class.forName(className).newInstance();
                                if (o instanceof InterpreterParserCustom)
                                    info.customParser.add((InterpreterParserCustom) o);
                            }
                            break;
                        case InterpreterParserAfter.HEADER_FLAG:
                            info.afterParser = new ArrayList<>();
                            String[] classNamesAfter = v.split(":");
                            for (String className : classNamesAfter) {
                                Object o = Class.forName(className).newInstance();
                                if (o instanceof InterpreterParserAfter)
                                    info.afterParser.add((InterpreterParserAfter) o);
                            }
                            break;
                        case SimpleObservable.TIME_HEADER_FLAG:
                            String[] time = v.split(":");
                            if (time.length == 2) {
                                info.timeLogFlag = new String(Base64.decode(time[0].replace("!", "="), Base64.DEFAULT));
                                info.lastTime = Long.parseLong(time[1]);
                            }
                            break;
                    }
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InstantiationException e) {
                    throw new IOException(e);
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
            }
        }
        info.json = split[split.length - 1];


        InterpreterParseBefore interpreterParseBefore = Bridge.getSetting().onBeforeParse();
        if (interpreterParseBefore != null) {
            if (info.beforeParser == null)
                info.beforeParser = new ArrayList<>();
            info.beforeParser.add(interpreterParseBefore);
        }


        InterpreterParseError interpreterParseError = Bridge.getSetting().onErrorParse();
        if (interpreterParseError != null) {
            if (info.errorParser == null)
                info.errorParser = new ArrayList<>();
            info.errorParser.add(interpreterParseError);
        }

        InterpreterParserCustom interpreterParserCustom = Bridge.getSetting().onCustomParse(dataClass);
        if (interpreterParserCustom != null) {
            if (info.customParser == null)
                info.customParser = new ArrayList<>();
            info.customParser.add(interpreterParserCustom);
        }

        InterpreterParserAfter interpreterParserAfter = Bridge.getSetting().onAfterParse();
        if (interpreterParserAfter != null) {
            if (info.afterParser == null)
                info.afterParser = new ArrayList<>();
            info.afterParser.add(interpreterParserAfter);
        }

        Log.out("getResponseContent.before json:" + string);
        Log.out("getResponseContent:" + info);
        return info;
    }

    /**
     * 可直接返回请求结果的字符序列
     * Created by xinjun on 2019/12/4 16:25
     */
    private class StringModelConverter implements Converter<ResponseBody, Str> {
        @Override
        public Str convert(@NonNull ResponseBody value) throws IOException {
            ResponseInfo responseContent = getResponseContent(value, String.class);
            Str str = new Str(responseContent.json);
            str.setCache(responseContent.isCache);
            logTimeIfNeed(responseContent, "requesting");
            return str;
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
            ResponseInfo responseInfo = getResponseContent(value, (Class) type);
            String jsonStr = responseInfo.json;

            logTimeIfNeed(responseInfo, "requesting");

            if (TextUtils.isEmpty(jsonStr))
                return null;

            if (responseInfo.errorParser != null && responseInfo.errorParser.size() > 0) {

                Error error = ChainUtil.doChain(false, new ChainUtil.Invoker<Error, InterpreterParseError, String>() {
                    @Override
                    public Error invoke(InterpreterParseError obj, String arg) {
                        try {
                            return obj.onParseError(arg);
                        } catch (Exception e) {
                            CommonUtil.throwError(Error.Code.PARSE_ERROR.code, "a error occur in InterpreterParseError.onParseError,class is " + obj.getClass().getName());
                        }
                        return null;
                    }
                }, responseInfo.errorParser, jsonStr);

                if (error != null) {
                    error.setCustomer(true);
                    CommonUtil.throwError(error);
                }
            }
            logTimeIfNeed(responseInfo, "custom error parse");

            if (responseInfo.beforeParser != null && responseInfo.beforeParser.size() > 0) {
                jsonStr = ChainUtil.doChain(true, new ChainUtil.Invoker<String, InterpreterParseBefore, String>() {
                    @Override
                    public String invoke(InterpreterParseBefore obj, String arg) {
                        try {
                            String ret = obj.onBeforeParse(arg);
                            if (TextUtils.isEmpty(ret))
                                return null;
                            return ret;
                        } catch (Exception e) {
                            CommonUtil.throwError(Error.Code.PARSE_CUSTOM.code, "a error occur in InterpreterParseBefore.onBeforeParse,class is" + obj.getClass().getName());
                        }
                        return null;
                    }
                }, responseInfo.beforeParser, jsonStr);

            }

            logTimeIfNeed(responseInfo, "modify json result");

            if (responseInfo.customParser != null && responseInfo.customParser.size() > 0) {
                T customParseData = ChainUtil.doChain(false, new ChainUtil.Invoker<T, InterpreterParserCustom, String>() {
                    @Override
                    public T invoke(InterpreterParserCustom obj, String arg) {
                        try {
                            @SuppressWarnings("unchecked")
                            T ret = (T) obj.onCustomParse(arg);
                            return ret;
                        } catch (Exception e) {
                            CommonUtil.throwError(Error.Code.PARSE_CUSTOM.code, "a error occur in InterpreterParserCustom.onCustomParse,class is" + obj.getClass().getName());
                        }
                        return null;
                    }
                }, responseInfo.customParser, jsonStr);

                if (customParseData != null) {
                    callAfterParse(responseInfo.afterParser, customParseData);
                    customParseData.setCache(responseInfo.isCache);
                    return customParseData;
                }

            }

            logTimeIfNeed(responseInfo, "custom parse");

            //frame work parse data
            Gson gson = gsonBuilder.create();
            T data = null;
            try {
                data = gson.fromJson(jsonStr, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            logTimeIfNeed(responseInfo, "Gson parse");
            Log.out("parse:data=" + data);
            if (data == null) {
                CommonUtil.throwError(Error.Code.PARSE_BEAN.code, "parse error");
            }

            boolean cacheResponse = responseInfo.isCache;
            Log.out("parse:is cache response=" + cacheResponse);
            if (data != null) {
                data.setCache(cacheResponse);
            }

            callAfterParse(responseInfo.afterParser, data);
            logTimeIfNeed(responseInfo, "after parse");

            return data;
        }
    }


    private void callAfterParse(List<InterpreterParserAfter> afterParser, Object data) {
        if (afterParser != null)
            for (InterpreterParserAfter after : afterParser)
                try {
                    //noinspection unchecked
                    after.onParseCompleted(data);
                } catch (ClassCastException ignore) {
                }
    }


    private long getCurrTime() {
        return System.currentTimeMillis();
    }

    private void logTimeIfNeed(ResponseInfo info, String reason) {
        if (!TextUtils.isEmpty(info.timeLogFlag)) {
            long currTime = getCurrTime();
            int useTime = (int) (currTime - info.lastTime);
            info.lastTime = currTime;
            Log.out(info.timeLogFlag + ":" + reason + "=" + useTime + "ms");
        }
    }


}
