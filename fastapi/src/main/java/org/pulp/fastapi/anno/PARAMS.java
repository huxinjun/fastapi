package org.pulp.fastapi.anno;

import org.pulp.fastapi.anno.PARAM;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 静态请求参数们
 * Created by xinjun on 2019/12/11 13:51
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface PARAMS {
    PARAM[] value();
}

