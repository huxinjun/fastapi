package org.pulp.fastapi.anno;

import org.pulp.fastapi.i.PageCondition;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义分页注解
 * Created by xinjun on 2019/12/11 13:51
 */
@Target({METHOD,TYPE})
@Retention(RUNTIME)
public @interface Page {
    Class<? extends PageCondition> value();
}

