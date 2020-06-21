package org.pulp.fastapi.extension;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.aichang.blackbeauty.base.net.util.UrlUtil;
import retrofit2.http.Query;

/**
 * api转换为url
 * 适用于图片或其他地址的转换
 * Created by xinjun on 2019/12/14 15:51
 */
public class StaticUrl {

    public String url() {
        return UrlUtil.map2url(mUrl, params);
    }

    void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    private String mUrl;

    private Map<String, String> params = new HashMap<>();

    /**
     * 组装url
     *
     * @param method method
     * @param args   args
     */
    public static void assemble(StaticUrl staticUrl, Method method, Object[] args) {
        Annotation[][] parameterAnnos = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnos.length; i++) {
            for (int j = 0; j < parameterAnnos[i].length; j++) {
                Annotation annotation = parameterAnnos[i][j];
                if (annotation instanceof Query) {
                    Query query = (Query) annotation;
                    String key = query.value();
                    String value = args[i].toString();
                    staticUrl.params.put(key, value);
                }
            }
        }
    }
}
