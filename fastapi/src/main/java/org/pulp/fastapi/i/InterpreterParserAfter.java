package org.pulp.fastapi.i;


import androidx.annotation.NonNull;

/**
 * after parse
 * Created by xinjun on 2020/6/23 16:51
 */
public interface InterpreterParserAfter<T> {


    String HEADER_FLAG = "AfterParser";

    /**
     * after parse
     */
    void onParseCompleted(@NonNull T bean);
}
