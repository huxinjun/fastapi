package org.pulp.fastapi.i;


import android.support.annotation.NonNull;

/**
 * custom parse
 * Created by xinjun on 2020/6/23 16:51
 */
public interface InterpreterParserCustom<T> {


    String HEADER_FLAG = "CustomParser";

    /**
     * custom parse
     *
     * @return null use framework parse
     */
    T onCustomParse(@NonNull String json);
}
