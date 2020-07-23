package org.pulp.fastapi.extension;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.pulp.fastapi.Bridge;
import org.pulp.fastapi.anno.Cache;
import org.pulp.fastapi.anno.MultiPath;
import org.pulp.fastapi.anno.OnAfterParse;
import org.pulp.fastapi.anno.OnBeforeParse;
import org.pulp.fastapi.anno.OnCustomParse;
import org.pulp.fastapi.anno.OnErrorParse;
import org.pulp.fastapi.anno.Page;
import org.pulp.fastapi.anno.Param;
import org.pulp.fastapi.anno.Params;
import org.pulp.fastapi.anno.PathParser;
import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.i.PathConverter;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.util.CommonUtil;
import org.pulp.fastapi.util.Log;
import org.pulp.fastapi.util.UrlUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

/**
 * 支持SimpleObservable的CallAdapter
 * Created by xinjun on 2019/12/4 16:37
 */
public class SimpleCallAdapter<R> implements CallAdapter<R, Object> {

    private CallAdapter<R, Object> realCallApdater;
    private Type observableType;
    private Class<?> rawType;
    private Annotation[] annotations;
    private PathConverter annoPathConverter;
    private Retrofit retrofit;
    private Class<?> apiClass;
    private List<Class<?>> parserBeforeClasses;
    private List<Class<?>> parserErrorClasses;
    private List<Class<?>> parserCustomClasses;
    private List<Class<?>> parserAfterClasses;

    public SimpleCallAdapter(CallAdapter<R, Object> realCallApdater, Type observableType
            , Class<?> rawType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit, Class<?> apiClass) {
        this.realCallApdater = realCallApdater;
        this.observableType = observableType;
        this.rawType = rawType;
        this.annotations = annotations;
        this.retrofit = retrofit;
        this.apiClass = apiClass;
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
            final AtomicReference<String> staticUrl = new AtomicReference<>();
            String path = findPath();
            Log.out("find path from method annotation:" + path);
            if (path == null)
                return adapt;
            if (TextUtils.isEmpty(path))
                return adapt;
            path = path.trim();


            if (adapt instanceof Observable) {
                Observable<R> observable = (Observable<R>) adapt;
                IOObservable IOObservable = new IOObservable(observable);
                Observable<R> observableOnMainThread = IOObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

                SimpleObservable<?> simpleObservable = null;
                if (rawType == SimpleListObservable.class)
                    simpleObservable = new SimpleListObservable(observableOnMainThread, observableType, annotations, retrofit, apiClass);
                else if (rawType == SequenceObservable.class)
                    simpleObservable = new SequenceObservable(observableOnMainThread, observableType, annotations, retrofit, apiClass);
                else if (rawType == SimpleObservable.class)
                    simpleObservable = new SimpleObservable(observableOnMainThread, observableType, annotations, retrofit, apiClass);
                else if (rawType == URL.class)
                    simpleObservable = new SimpleObservable(IOObservable, observableType, annotations, retrofit, apiClass);

                IOObservable.setListener(simpleObservable);

                final Map<String, String> annoParams = new LinkedHashMap<>();
                parseOnBeforeParseAnno(findAnnoByClass(annotations, OnBeforeParse.class));
                parseOnErrorParseAnno(findAnnoByClass(annotations, OnErrorParse.class));
                parseOnCustomParseAnno(findAnnoByClass(annotations, OnCustomParse.class));
                parseOnAfterParseAnno(findAnnoByClass(annotations, OnAfterParse.class));
                parsePageAnno(findAnnoByClass(annotations, Page.class), simpleObservable);
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Param) {
                            Map<String, String> paramMap = parseParamAnno((Param) annotation);
                            if (paramMap != null)
                                annoParams.putAll(paramMap);
                        } else if (annotation instanceof Params) {
                            Map<String, String> paramsMap = parseParamsAnno((Params) annotation);
                            if (paramsMap != null)
                                annoParams.putAll(paramsMap);
                        } else if (annotation instanceof MultiPath) {
                            parseMultiPathAnno((MultiPath) annotation, simpleObservable);
                        } else if (annotation instanceof PathParser) {
                            parsePathParserAnno((PathParser) annotation);
                        } else if (annotation instanceof Cache) {
                            parseCacheAnno((Cache) annotation, simpleObservable);
                        }
                    }
                }


                if (simpleObservable == null)
                    return adapt;

                simpleObservable.setPath(path);
                final SimpleObservable<?> finalSimpleObservable = simpleObservable;
                final String finalPath = path;
                simpleObservable.setRequestRebuilder(new SimpleObservable.RequestRebuilder() {
                    @Override
                    public void onModify(Request.Builder requestBuilder, Map<String, String> extraParams) {

                        Request request = requestBuilder.build();

                        boolean isPostMethod = "post".equalsIgnoreCase(request.method());

                        Map<String, String> params = new LinkedHashMap<>();
                        Map<String, String> queryParams = requestParam2map(request); // 此处为原始request中的参数
                        Map<String, String> baseParams = Bridge.getSetting().onGetCommonParams();
                        // 基础参数
                        if (baseParams != null)
                            params.putAll(baseParams);

                        //注解参数
                        params.putAll(annoParams);

                        //分页参数
                        if (extraParams != null)
                            params.putAll(extraParams);

                        String newPath = finalPath;
                        String convertUrl = null;
                        if (finalSimpleObservable instanceof SequenceObservable)
                            newPath = ((SequenceObservable) finalSimpleObservable).getCurrPath();

                        boolean needConvertPath = !finalPath.startsWith("http") && !finalPath.startsWith("/");
                        if (needConvertPath) {
                            convertUrl = pathConvert(newPath);
                            //check url valid
                            try {
                                assert convertUrl != null;
                                new Request.Builder().url(convertUrl).build();
                            } catch (IllegalArgumentException e) {
                                CommonUtil.throwError(Error.ERR_CRASH, "convert url invalid,path="
                                        + newPath
                                        + ",url="
                                        + convertUrl
                                        + ",PathConverter="
                                        + (getPathConvert() == null ? "null" : getPathConvert().getClass().getName())
                                );
                            }
                        }

                        Log.out("before url:" + convertUrl);

                        // 开始根据不同请求方式构建新Request 并返回
                        if (!isPostMethod) {
                            //GET
                            //重组的get请求的request对象必须有params,不然请求不会携带url中的参数,参见:
                            //RealConnection.newCodec-->Http2Codec.http2HeadersList-->RequestLine.requestPath
                            if (!TextUtils.isEmpty(convertUrl)) {
                                params.putAll(queryParams);
                                convertUrl = UrlUtil.map2url(convertUrl, params);
                            } else
                                convertUrl = UrlUtil.map2url(request.url().toString(), params);

                            Log.out("after url:" + convertUrl);
                            requestBuilder.url(convertUrl);
                        } else {
                            //POST
                            //query param should append to url
                            if (!TextUtils.isEmpty(convertUrl))
                                convertUrl = UrlUtil.map2url(convertUrl, queryParams);
                            else
                                convertUrl = request.url().toString();
                            //reconstruct post request and add post param body
                            assemblePostRequest(requestBuilder, convertUrl, params);
                        }

                        //user parse support
                        if (!TextUtils.isEmpty(classArr2str(parserBeforeClasses)))
                            requestBuilder.addHeader(InterpreterParseBefore.HEADER_FLAG, classArr2str(parserBeforeClasses));
                        if (!TextUtils.isEmpty(classArr2str(parserErrorClasses)))
                            requestBuilder.addHeader(InterpreterParseError.HEADER_FLAG, classArr2str(parserErrorClasses));
                        if (!TextUtils.isEmpty(classArr2str(parserCustomClasses)))
                            requestBuilder.addHeader(InterpreterParserCustom.HEADER_FLAG, classArr2str(parserCustomClasses));
                        if (!TextUtils.isEmpty(classArr2str(parserAfterClasses)))
                            requestBuilder.addHeader(InterpreterParserAfter.HEADER_FLAG, classArr2str(parserAfterClasses));


                        if (rawType == URL.class) {
                            requestBuilder.addHeader("StaticUrl", "true");
                            staticUrl.set(convertUrl);
                        }
                    }
                });


                if (rawType == URL.class) {
                    simpleObservable.subscribeActual(null);
                    if (TextUtils.isEmpty(staticUrl.get()))
                        return null;
                    return new URL(staticUrl.get());
                }
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


    private void parseMultiPathAnno(MultiPath anno, SimpleObservable<?> simpleObservable) {
        String[] value = anno.value();
        Log.out("parseMultiPathAnno.value=" + Arrays.toString(value));
        if (simpleObservable instanceof SequenceObservable) {
            SequenceObservable sequenceObservable = (SequenceObservable) simpleObservable;
            sequenceObservable.setPaths(value);
        }
    }


    @SuppressWarnings("unchecked")
    private void parsePageAnno(@Nullable Page pageAnno, SimpleObservable<?> simpleObservable) {
        if (!(simpleObservable instanceof SimpleListObservable))
            return;
        try {
            PageCondition pageCondition;
            String from = "global setting";

            pageCondition = Bridge.getSetting().onGetPageCondition();


            Page classAnno = apiClass.getAnnotation(Page.class);
            if (classAnno != null) {
                Class<? extends PageCondition> value = classAnno.value();
                pageCondition = value.newInstance();
                from = "class";
            }

            if (pageAnno != null) {
                Class<? extends PageCondition> value = pageAnno.value();
                pageCondition = value.newInstance();
                from = "method";
            }

            if (pageCondition == null)
                return;
            Log.out("use " + from + " pageCondition:" + pageCondition);
            ((SimpleListObservable) simpleObservable).pageCondition(pageCondition);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private Map<String, String> parseParamAnno(Param paramAnno) {
        final String[] value = paramAnno.value();
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

    private Map<String, String> parseParamsAnno(Params paramsAnno) {
        Map<String, String> ret = new HashMap<>();
        Param[] value = paramsAnno.value();
        for (Param paramAnno : value) {
            Map<String, String> paramMap = parseParamAnno(paramAnno);
            if (paramMap != null)
                ret.putAll(paramMap);
        }
        return ret;
    }


    private void parsePathParserAnno(PathParser anno) {
        Class<? extends PathConverter> value = anno.value();
        Log.out("parsePathParserAnno.value=" + value);
        try {
            this.annoPathConverter = value.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


    private void parseOnBeforeParseAnno(@Nullable OnBeforeParse anno) {

        parserBeforeClasses = new ArrayList<>();

        if (anno != null) {
            Class<? extends InterpreterParseBefore> methodParser = anno.value();
            Log.out("parseOnBeforeParseAnno.methodParser=" + methodParser);
            parserBeforeClasses.add(methodParser);
        }

        OnBeforeParse classParser = apiClass.getAnnotation(OnBeforeParse.class);
        if (classParser != null) {
            Log.out("parseOnBeforeParseAnno.classParser=" + classParser);
            parserBeforeClasses.add(classParser.value());
        }
    }

    private void parseOnErrorParseAnno(@Nullable OnErrorParse anno) {

        parserErrorClasses = new ArrayList<>();

        if (anno != null) {
            Class<? extends InterpreterParseError> methodParser = anno.value();
            Log.out("parseOnErrorParseAnno.methodParser=" + methodParser);
            parserErrorClasses.add(methodParser);
        }

        OnErrorParse classParser = apiClass.getAnnotation(OnErrorParse.class);
        if (classParser != null) {
            Log.out("parseOnErrorParseAnno.classParser=" + classParser);
            parserErrorClasses.add(classParser.value());
        }
    }

    private void parseOnCustomParseAnno(@Nullable OnCustomParse anno) {

        parserCustomClasses = new ArrayList<>();

        if (anno != null) {
            Class<? extends InterpreterParserCustom> methodParser = anno.value();
            Log.out("parseOnCustomParseAnno.methodParser=" + methodParser);
            parserCustomClasses.add(methodParser);
        }

        OnCustomParse classParser = apiClass.getAnnotation(OnCustomParse.class);
        if (classParser != null) {
            Log.out("parseOnCustomParseAnno.classParser=" + classParser);
            parserCustomClasses.add(classParser.value());
        }
    }

    private void parseOnAfterParseAnno(@Nullable OnAfterParse anno) {

        parserAfterClasses = new ArrayList<>();

        if (anno != null) {
            Class<? extends InterpreterParserAfter> methodParser = anno.value();
            Log.out("parseOnAfterParseAnno.methodParser=" + methodParser);
            parserAfterClasses.add(methodParser);
        }

        OnAfterParse classParser = apiClass.getAnnotation(OnAfterParse.class);
        if (classParser != null) {
            Log.out("parseOnAfterParseAnno.classParser=" + classParser);
            parserAfterClasses.add(classParser.value());
        }
    }

    private void parseCacheAnno(Cache anno, SimpleObservable<?> simpleObservable) {
        String value = anno.value().getValue();
        Log.out("parseCacheAnno.value=" + value);
        if (simpleObservable != null)
            simpleObservable.cachePolicy(value);
    }

    private String classArr2str(List<Class<?>> classList) {
        if (classList == null || classList.size() == 0)
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (Class<?> clazz : classList)
            if (clazz != null)
                stringBuilder.append(clazz.getName()).append(":");


        if (stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();

    }


    private <T> T findAnnoByClass(Annotation[] annotations, Class<T> annoClass) {
        if (annotations == null || annotations.length == 0)
            return null;
        for (Annotation anno : annotations)
            if (anno != null && anno.annotationType() == annoClass)
                //noinspection unchecked
                return (T) anno;
        return null;
    }


    /**
     * 将path转换为url
     *
     * @param path path
     * @return 接口地址
     */
    private String pathConvert(String path) {
        if (TextUtils.isEmpty(path))
            return null;
        if (path.startsWith("http") || path.startsWith("/")) {
            return path;
        }

        PathConverter pathConverter = getPathConvert();
        String url = null;
        if (pathConverter != null)
            url = pathConverter.onConvert(path);
        if (TextUtils.isEmpty(url))
            return path;
        Log.out("pathConvert.path=" + path + "--->url=" + url);
        return url;
    }

    private PathConverter getPathConvert() {
        //优先使用方法注解的path converter
        return annoPathConverter != null ? annoPathConverter : Bridge.getSetting().onGetPathConverter();
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
