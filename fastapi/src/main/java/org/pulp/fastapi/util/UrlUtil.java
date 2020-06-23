package org.pulp.fastapi.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 关于的url的工具类
 * Created by xinjun on 2019/12/2 14:19
 */
public class UrlUtil {


//    /**
//     * 添加基础通用的参数
//     *
//     * @param url url
//     * @return 添加了参数的url
//     */
//    public static String appendBaseParam(String url) {
//        if (TextUtils.isEmpty(url))
//            return "";
//
//        //url添加协议
//        if (!url.startsWith("http"))
//            url = "http://" + url;
//
//        return map2url(url, getBaseParamMap());
//    }
//
//
//    /**
//     * 获取通用参数map
//     */
//    public static Map<String, String> getBaseParamMap() {
//        Map<String, String> param = new LinkedHashMap<>();
//        Bundle appMeta = KShareApplication.getApplicationMetaData();
//        String ver = KShareApplication.getPackInfo();
//        boolean isreal = false;
//        if (ver.startsWith("4.")) {
//            isreal = true;
//        }
//        if (ver.startsWith("3.9.")) {
//            ver = "4.0.0";
//        }
//        if (isreal) {
//            param.put("real_ver", ver);
//        }
//        param.put("base_version", ver);
//        param.put("ver", ver);
//        param.put("base_platform", "android");
//        param.put("base_machine", SystemDevice.getInstance().getSystemMachine());
//
//
//        if (appMeta != null && appMeta.getString("market") != null) {
//            param.put("base_market", appMeta.getString("market"));
//        } else {
//            param.put("base_market", "no_market");
//        }
//        String first = PreferencesUtils.loadPrefString(KShareApplication.getInstance(),
//                PreferencesUtils.FIRST_MARKET, null);
//        if (!TextUtils.isEmpty(first)) {
//            param.put("first_market", first);
//        }
//        if (TextUtils.isEmpty(SystemDevice.getInstance().getDeviceId())) {
//            param.put("device_id", "0");
//        } else {
//            param.put("device_id", SystemDevice.getInstance().getDeviceId());
//        }
//
//        param.put("flag", "newbase");
//
//
//        param.put("__API__[charset]", "utf-8");
//        param.put("__API__[output]", "json");
//        param.put("__API__[app_key]", "3856789339");
//        param.put("__API__[app_secret]", "4a09feb097337960c380c8f4c2666a04");
//
//
//        try {
//            if (!Session.getCurrentAccount().isAnonymous()) {
//                String sig = Session.getCurrentAccount().mToken;
//                param.put("sig", sig);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return param;
//    }

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
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
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
    public static String map2url(String url, Map<String, String> map) {
        if (map == null || map.size() == 0)
            return url;

        HttpUrl.Builder httpUrlBuilder = new Request.Builder().url(url).build().url().newBuilder();

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            httpUrlBuilder.addQueryParameter(next.getKey(), next.getValue());
        }
        return httpUrlBuilder.toString();


//        StringBuilder paramBuilder = new StringBuilder(url);
//        if (!url.contains("?"))
//            paramBuilder.append("?");
//        if (paramBuilder.charAt(paramBuilder.length() - 1) != '&' && paramBuilder.indexOf("=") > 0)
//            paramBuilder.append("&");
//
//
//        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, String> next = iterator.next();
//            paramBuilder.append(next.getKey());
//            paramBuilder.append("=");
//            paramBuilder.append(next.getValue());
//            paramBuilder.append("&");
//        }
//        paramBuilder.deleteCharAt(paramBuilder.length() - 1);
//
//        return paramBuilder.toString();
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


//        try {
//            String s = url.split("\\?")[1];
//            String[] split = s.split("&");
//            for (String str : split) {
//                String[] p = str.split("=");
//                result.put(p[0], p[1]);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return result;


    }
}
