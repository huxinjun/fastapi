package org.pulp.fastapi.extension;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pulp.fastapi.ApiClient;
import org.pulp.fastapi.anno.CONFIG;
import org.pulp.fastapi.anno.PAGE;
import org.pulp.fastapi.anno.PARAM;
import org.pulp.fastapi.anno.PARAMS;
import org.pulp.fastapi.page.DeffaultPageCondition;
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
            String keyname = findUrlKey();
            ULog.out("find keyname from method annotation:" + keyname);
            if (TextUtils.isEmpty(keyname))
                return adapt;


            if (adapt instanceof Observable) {
                Observable<R> observable = (Observable<R>) adapt;
                IOObservable IOObservable = new IOObservable(observable);
                Observable<R> observableOnMainThread = IOObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

                SimpleObservable<?> simpleObservable = null;
                StaticUrl staticUrl = null;
                if (rawType == SimpleListObservable.class)
                    simpleObservable = new SimpleListObservable(observableOnMainThread, observableType, annotations);
                else if (rawType == ConfigObservable.class)
                    simpleObservable = new ConfigObservable(observableOnMainThread, observableType, annotations);
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
                        } else if (annotation instanceof CONFIG) {
                            parseConfigAnno((CONFIG) annotation, simpleObservable);
                        }
                    }
                }
                if (staticUrl != null) {
                    String fullReqUrl = assembleReqUrl(keyname);
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

                if (simpleObservable instanceof SimpleListObservable) {
                    SimpleListObservable listObservable = (SimpleListObservable) simpleObservable;
                    if (listObservable.mPageCondition == null)
                        listObservable.pageCondition(new DeffaultPageCondition());
                }


                if (simpleObservable == null)
                    return adapt;

                simpleObservable.setUrlkey(keyname);
                SimpleObservable<?> finalSimpleObservable = simpleObservable;
                simpleObservable.setModifyUrlCallback((requestBuilder, extraParams) -> {

                    Request request = requestBuilder.build();

                    if (extraParams != null && extraParams.size() == 1 && extraParams.get(DeffaultPageCondition.NO_MORE_DATA) != null) {
                        ULog.out("adapt.NO_MORE_DATA!!!");
                        finalSimpleObservable.cancelNextRequest();
                    }
                    boolean isPostMethod = "post".equalsIgnoreCase(request.method());

                    //FIXME: 此处出现一种特殊情况: request中的queryParams为空的时候，后面在okhttp发送请求的时候
                    //      会不带url 后面的get参数。仅仅传给请求的url path
                    //      例如：http://api.xxx.cn?xx=xx&yy=yy 当QueryParams为空的时候，发送的请求变为http://api.xxx.cn
                    //      所以这里的处理方案是把BaseParams放到QueryParams中，因此这里需要重新生成request，又为了
                    //      后面缓存需要的url（猜测）其他的参数还是靠追加到url path后面来做。这样就避免了请求参数丢失的情况。
                    //      原因是，当有QueryParams时，请求的时候就会把后面参数追加进来。具体看RequestLine, HttpUrl的源码
                    //      public static String requestPath(HttpUrl url) {
                    //        String path = url.encodedPath();
                    //        String query = url.encodedQuery();
                    //        return query != null ? path + '?' + query : path;
                    //    }
                    //     简单的处理，构造好请求的url，重新生成新的Request返回给后面进行处理

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
                    if (finalSimpleObservable instanceof ConfigObservable)
                        fullReqUrl = assembleReqUrl(((ConfigObservable) finalSimpleObservable).getCurrUrl());
                    else
                        fullReqUrl = assembleReqUrl(keyname);

                    Map<String, String> urlParams = UrlUtil.url2map(fullReqUrl); // 请求地址中的参数

                    String finalUrl = fullReqUrl;
                    if (fullReqUrl == null) {
                        Log.d("fullRequrl", "url: " + fullReqUrl);
                    }
                    // 开始根据不同请求方式构建新Request 并返回
                    if (!isPostMethod) {
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


    private String findUrlKey() {
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


    private void parseConfigAnno(CONFIG configAnno, SimpleObservable<?> simpleObservable) {
        String[] value = configAnno.value();
        ULog.out("parseConfigAnno.value=" + Arrays.toString(value));
        if (simpleObservable instanceof ConfigObservable) {
            ConfigObservable configObservable = (ConfigObservable) simpleObservable;
            configObservable.setUrls(value);
        }
    }


    @SuppressWarnings("unchecked")
    private void parsePageAnno(PAGE pageAnno, SimpleObservable<?> simpleObservable) {
        Class<? extends SimpleListObservable.PageCondition> value = pageAnno.value();
        ULog.out("adapt.PAGE.value=:" + value);
        try {
            SimpleListObservable.PageCondition pageCondition = value.newInstance();
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
     * 用keyname取出urlkey中的地址
     * 优先使用服务器请求的url
     *
     * @param urlkeyName keyname
     * @return 接口地址
     */
    private String assembleReqUrl(String urlkeyName) {
        ULog.out("assembleReqUrl.urlkeyName:" + urlkeyName);
        if (TextUtils.isEmpty(urlkeyName))
            return null;
        if (urlkeyName.startsWith("http")) {
            return urlkeyName;
        }
        String result = null;
        if (ApiClient.GlobalUrlkey != null && ApiClient.GlobalUrlkey.downloadUrls != null) {
            ULog.out("assembleReqUrl.use downloadUrls:" + ApiClient.GlobalUrlkey.downloadUrls.get(urlkeyName));
            result = ApiClient.GlobalUrlkey.downloadUrls.get(urlkeyName);
        }
        if (result == null) {
            ULog.out("assembleReqUrl.use defaultUrls:" + UrlKey.defaultUrls.get(urlkeyName));
            result = UrlKey.defaultUrls.get(urlkeyName);
        }
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
