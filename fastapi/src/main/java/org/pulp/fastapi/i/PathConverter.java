package org.pulp.fastapi.i;

/**
 * 将api方法上get,post等等注解的path转换为url
 * Created by xinjun on 2020/6/22 18:00
 */
public interface PathConverter {

    /**
     * 自定义path
     * 以http,斜杠开始的不会转换
     *
     * @param path url path
     * @return full url
     */
    String onConvert(String path);
}
