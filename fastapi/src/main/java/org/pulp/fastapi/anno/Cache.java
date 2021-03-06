package org.pulp.fastapi.anno;

import org.pulp.fastapi.i.CachePolicy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 缓存策略
 * Created by xinjun on 2020/6/24 19:17
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Cache {
    CachePolicy value();
}

