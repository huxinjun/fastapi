package org.pulp.fastapi.i;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.model.Error;

import io.reactivex.annotations.NonNull;

/**
 * 自定义解析器
 * Created by xinjun on 2020/6/23 16:51
 */
public interface Parser<T> {

    /**
     * 解析前
     *
     * @param json 待解析json
     * @return 修改后的json
     */
    @NonNull
    String onBeforeParse(@NonNull String json);


    /**
     * 解析错误信息
     *
     * @param json json obj
     * @return 错误 return null if no error
     */
    Error onParseError(@NonNull String json) throws Exception;


    /**
     * 完全自定义解析,不使用框架提供的方法
     *
     * @return null use framework parse
     */
    T onCustomParse(@NonNull String json);
}
