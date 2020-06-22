package org.pulp.fastapi.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * config url
 * Created by xinjun on 2019/12/12 21:32
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface MULTI_PATH {
    String[] value();
}

