package org.pulp.fastapi.extension;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pulp.fastapi.Get;
import org.pulp.fastapi.anno.MULTI_PATH;
import org.pulp.fastapi.anno.PAGE;
import org.pulp.fastapi.anno.PARAM;
import org.pulp.fastapi.anno.PARAMS;
import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.i.PathConverter;
import org.pulp.fastapi.util.ULog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.aichang.blackbeauty.base.net.util.UrlUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

/**
 * 支持SimpleObservable的CallAdapter
 * Created by xinjun on 2019/12/4 16:37
 */
public class AichangCallAdapter<R> implements CallAdapter<R, Object> {

    private CallAdapter<R, Object> realCallApdater;
    private Type observableType;
    private Class<?> rawType;
    private Annotation[] annotations;

    public AichangCallAdapter(CallAdapter<R, Object> realCallApdater, Type observableType
            , Class<?> rawType, @NonNull Annotation[] annotations) {
        this.realCallApdater = realCallApdater;
        this.observableType = observableType;
        this.rawType = rawType;
        this.annotations = annotations;
    }

    @Override
    public Type responseType() {
        return observableType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object adapt(@NonNull Call<R> call) {
        Object adapt = realCallApdater.adapt(call);//BodyObservable

        //动态url
        try {
            String path = findPath();
            ULog.out("find path from method annotation:" + path);
            if (TextUtils.isEmpty(path))
                return adapt;


            if (adapt instanceof Observable) {
                Observable<R> observable = (Observable<R>) adapt;
                IOObservable IOObservable = new IOObservable(observable);
                Observable<R> observableOnMainThread = IOObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

                SimpleObservable<?> simpleObservable = null;
                StaticUrl staticUrl = null;
                if (rawType == SimpleListObservable.class)
                    simpleObservable = new SimpleListObservable(observableOnMainThread, observableType, annotations);
                else if (rawType == SequenceObservable.class)
                    simpleObservable = new SequenceObservable(observableOnMainThread, observableType, annotations);
                else if (rawType == SimpleObservable.class)
                    simpleObservable = new SimpleObservable(observableOnMainThread, observableType, annotations);
                else if (rawType == StaticUrl.class)
                    staticUrl = new StaticUrl();

                IOObservable.setListener(simpleObservable);

                Map<String, String> annoParams = new LinkedHashMap<>();
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof PAGE) {
                            parsePageAnno((PAGE) annotation, simpleObservable);
                        } else if (annotation instanceof PARAM) {
                            Map<String, String> paramMap = parseParamAnno((PARAM) annotation);
                            if (paramMap != null)
                                annoParams.putAll(paramMap);

                        } else if (annotation instanceof PARAMS) {
                            Map<String, String> paramsMap = parseParamsAnno((PARAMS) annotation);
                            if (paramsMap != null)
                                annoParams.putAll(paramsMap);
                        } else if (annotation instanceof MULTI_PATH) {
                            parseConfigAnno((MULTI_PATH) annotation, simpleObservable);
                        }
                    }
                }
                if (staticUrl != null) {
                    String fullReqUrl = assembleReqUrl(path);
                    Map<String, String> params = new LinkedHashMap<>();
                    Map<String, String> baseParams = UrlUtil.getBaseParamMap();
                    if (baseParams != null)
                        params.putAll(baseParams);
                    params.putAll(annoParams);
                    String finalUrl = UrlUtil.map2url(fullReqUrl, params);
                    ULog.out("adapt.staticUrl:" + finalUrl);
                    ULog.out("adapt.base params:" + UrlUtil.map2str(baseParams));
                    ULog.out("adapt.anno params:" + UrlUtil.map2str(annoParams));
                    staticUrl.setUrl(finalUrl);
                    return staticUrl;
                }

                if (simpleObservable == null)
                    return adapt;

                simpleObservable.setPath(path);
                SimpleObservable<?> finalSimpleObservable = simpleObservable;
                simpleObservable.setModifyUrlCallback((requestBuilder, extraParams) -> {

                    Request request = requestBuilder.build();

                    boolean isPostMethod = "post".equalsIgnoreCase(request.method());

                    Map<String, String> params = new LinkedHashMap<>();
                    Map<String, String> queryParams = requestParam2map(request); // 此处为原始request中的参数
                    Map<String, String> baseParams = UrlUtil.getBaseParamMap();
                    // 基础参数
                    if (baseParams != null) {
                        params.putAll(baseParams);
                    }
                    //注解参数
                    params.putAll(annoParams);
                    //方法参数
                    if (queryParams != null)
                        params.putAll(queryParams);
                    //分页参数
                    if (extraParams != null)
                        params.putAll(extraParams);
                    String fullReqUrl; // 通过keyname获取请求的真实地址，构造真实的请求地址
                    if (finalSimpleObservable instanceof SequenceObservable)
                        fullReqUrl = assembleReqUrl(((SequenceObservable) finalSimpleObservable).getCurrUrl());
                    else
                        fullReqUrl = assembleReqUrl(path);

                    String finalUrl = fullReqUrl;
                    if (fullReqUrl == null) {
                        Log.d("fullRequrl", "url: " + fullReqUrl);
                    }
                    // 开始根据不同请求方式构建新Request 并返回
                    if (!isPostMethod) {
                        //重组的get请求的request对象必须有params,不然请求不会携带url中的参数,参见:
                        //RealConnection.newCodec-->Http2Codec.http2HeadersList-->RequestLine.requestPath
                        finalUrl = UrlUtil.map2url(fullReqUrl, params);
                        requestBuilder.url(finalUrl);
                    } else {
                        //query param should append to url
                        if (queryParams != null && queryParams.size() > 0)
                            finalUrl = UrlUtil.map2url(fullReqUrl, queryParams);
                        //reconstruct post request and add post param body
                        assemblePostRequest(requestBuilder, finalUrl, params);
                    }
                });
                return simpleObservable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return adapt;
    }


    private String findPath() {
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof GET) {
                    GET anno = (GET) annotation;
                    return anno.value();

                } else if (annotation instanceof POST) {
                    POST anno = (POST) annotation;
                    return anno.value();

                } else if (annotation instanceof HTTP) {
                    HTTP anno = (HTTP) annotation;
                    return anno.path();
                }
            }
        }
        return null;
    }


    private void parseConfigAnno(MULTI_PATH MULTIPATHAnno, SimpleObservable<?> simpleObservable) {
        String[] value = MULTIPATHAnno.value();
        ULog.out("parseConfigAnno.value=" + Arrays.toString(value));
        if (simpleObservable instanceof SequenceObservable) {
            SequenceObservable sequenceObservable = (SequenceObservable) simpleObservable;
            sequenceObservable.setUrls(value);
        }
    }


    @SuppressWarnings("unchecked")
    private void parsePageAnno(PAGE pageAnno, SimpleObservable<?> simpleObservable) {
        Class<? extends PageCondition> value = pageAnno.value();
        ULog.out("adapt.PAGE.value=:" + value);
        try {
            PageCondition pageCondition = value.newInstance();
            ((SimpleListObservable) simpleObservable).pageCondition(pageCondition);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private Map<String, String> parseParamAnno(PARAM paramAnno) {
        String[] value = paramAnno.value();
        switch (value.length) {
            case 0:
                return null;
            case 1:
                return new HashMap<String, String>() {
                    {
                        if (!TextUtils.isEmpty(value[0]))
                            put(value[0], "");
                    }
                };
            default:
                return new HashMap<String, String>() {
                    {
                        if (!TextUtils.isEmpty(value[0]))
                            put(value[0], value[1]);
                    }
                };
        }
    }

    private Map<String, String> parseParamsAnno(PARAMS paramsAnno) {
        Map<String, String> ret = new HashMap<>();
        PARAM[] value = paramsAnno.value();
        for (PARAM paramAnno : value) {
            Map<String, String> paramMap = parseParamAnno(paramAnno);
            if (paramMap != null)
                ret.putAll(paramMap);
        }
        return ret;
    }


    /**
     * 将path转换为url
     *
     * @param path path
     * @return 接口地址
     */
    private String assembleReqUrl(String path) {
        ULog.out("assembleReqUrl.path:" + path);
        if (TextUtils.isEmpty(path))
            return null;
        if (path.startsWith("http")) {
            return path;
        }
        String result = path;
        if (path.startsWith("/") || path.startsWith("http"))
            return result;
        PathConverter pathConverter = Get.getPathConverter();
        if (pathConverter != null)
            result = pathConverter.onConvert(path);
        return result;
    }

    private Map<String, String> requestParam2map(Request request) {
        Map<String, String> result = new HashMap<>();
        if (request == null)
            return result;
        HttpUrl url = request.url();

        for (int i = 0; i < url.querySize(); i++) {
            String name = url.queryParameterName(i);
            String value = url.queryParameterValue(i);
            result.put(name, value);
        }
        return result;
    }

    private void assemblePostRequest(Request.Builder builder, String url, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            builder.url(url);
            return;
        }

        Request request = builder.build();

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if (request.body() instanceof FormBody) {
            FormBody formBody = (FormBody) request.body();
            for (int i = 0; i < formBody.size(); i++) {
                String name = formBody.name(i);
                String value = formBody.value(i);
                params.put(name, value);
            }
        }

        for (Map.Entry<String, String> next : params.entrySet())
            bodyBuilder.add(next.getKey(), next.getValue());

        FormBody newBody = bodyBuilder.build();
        builder.url(url).post(newBody);
    }

}
