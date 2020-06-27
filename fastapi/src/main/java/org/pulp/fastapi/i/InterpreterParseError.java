package org.pulp.fastapi.i;


import android.support.annotation.NonNull;

import org.pulp.fastapi.model.Error;


/**
 * parse error
 * Created by xinjun on 2020/6/23 16:51
 */
public interface InterpreterParseError {

    String HEADER_FLAG = "ErrorParser";

    /**
     * user parse error
     *
     * @param json a json
     * @return a error,null if no error
     */
    Error onParseError(@NonNull String json) throws Exception;
}
