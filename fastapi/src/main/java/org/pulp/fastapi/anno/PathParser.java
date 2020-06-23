package org.pulp.fastapi.anno;


import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.i.Parser;
import org.pulp.fastapi.i.PathConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义路径解析
 * Created by xinjun on 2020/6/23 13:59
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface PathParser {
    Class<? extends PathConverter> value();
}

