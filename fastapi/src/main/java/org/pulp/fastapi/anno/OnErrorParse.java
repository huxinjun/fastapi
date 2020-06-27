package org.pulp.fastapi.anno;


import org.pulp.fastapi.i.InterpreterParseError;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * error parse annotation
 * Created by xinjun on 2020/6/23 13:59
 */
@Target({METHOD,TYPE})
@Retention(RUNTIME)
public @interface OnErrorParse {
    Class<? extends InterpreterParseError> value();
}

