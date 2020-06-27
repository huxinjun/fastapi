package org.pulp.fastapi.anno;


import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 基地址,可配置在申明API的类上,类中所有的方法将使用此基地址
 * Created by xinjun on 2020/6/24 19:17
 */
@Target({TYPE,METHOD})
@Retention(RUNTIME)
public @interface BaseUrl {
    @NonNull String value();
}

