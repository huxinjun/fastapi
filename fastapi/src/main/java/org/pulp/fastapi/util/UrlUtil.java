package org.pulp.fastapi.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 关于的url的工具类
 * Created by xinjun on 2019/12/2 14:19
 */
public class UrlUtil {

    /**
     * 参数map转为str,log用
     *
     * @param map 参数
     * @return log str
     */
    public static String map2str(Map<String, String> map) {
        if (map == null || map.size() == 0)
            return null;
        StringBuilder paramBuilder = new StringBuilder();
        for (Map.Entry<String, String> next : map.entrySet()) {
            paramBuilder.append(next.getKey());
            paramBuilder.append("=");
            paramBuilder.append(next.getValue());
            paramBuilder.append(",");
        }
        paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        return paramBuilder.toString();
    }


    /**
     * 将参数追加到url
     *
     * @param url baseurl
     * @param map 参数
     * @return 完整的url
     */
    public static String map2url(String url, Map<String, String> map) throws IllegalArgumentException {
        if (map == null || map.size() == 0)
            return url;


        HttpUrl.Builder httpUrlBuilder = new Request.Builder().url(url).build().url().newBuilder();

        for (Map.Entry<String, String> next : map.entrySet()) {
            httpUrlBuilder.addQueryParameter(next.getKey(), next.getValue());
        }
        return httpUrlBuilder.toString();
    }


    /**
     * url提取参数为map
     *
     * @param url url
     * @return 参数map
     */
    public static Map<String, String> url2map(String url) {
        Map<String, String> result = new HashMap<>();
        if (TextUtils.isEmpty(url))
            return result;

        HttpUrl httpUrl = new Request.Builder().url(url).build().url();
        httpUrl.querySize();

        for (String key : httpUrl.queryParameterNames())
            result.put(key, httpUrl.queryParameter(key));

        return result;


    }
}
