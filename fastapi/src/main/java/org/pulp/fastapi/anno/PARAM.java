package org.pulp.fastapi.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 静态请求参数
 * Created by xinjun on 2019/12/11 13:51
 */
@Target({METHOD,ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface PARAM {
    String[] value();
}

