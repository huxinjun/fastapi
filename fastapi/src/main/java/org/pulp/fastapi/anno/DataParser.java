package org.pulp.fastapi.anno;


import org.pulp.fastapi.i.Parser;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义数据解析
 * Created by xinjun on 2020/6/23 13:59
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface DataParser {
    Class<? extends Parser> value();
}

