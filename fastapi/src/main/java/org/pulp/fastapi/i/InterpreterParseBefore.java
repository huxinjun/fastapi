package org.pulp.fastapi.i;


import io.reactivex.annotations.NonNull;

/**
 * before parse json
 * Created by xinjun on 2020/6/23 16:51
 */
public interface InterpreterParseBefore {

    String HEADER_FLAG = "BeforeParser";

    /**
     * before parse json
     *
     * @param json a json
     * @return a json after modify
     */
    @NonNull
    String onBeforeParse(@NonNull String json);
}
