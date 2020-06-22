package org.pulp.fastapi.i;

import java.util.Map;

import androidx.annotation.Nullable;

/**
 * 自定义分页回调
 * Created by xinjun on 2019/12/9 11:13
 */
public interface PageCondition<T> {
    /**
     * 下一页数据
     *
     * @param data 当前数据
     * @return 要添加在请求中的参数
     */
    Map<String, String> nextPage(@Nullable T data);

    /**
     * 上一页数据
     *
     * @param data 当前数据
     * @return 要添加在请求中的参数
     */
    Map<String, String> prePage(@Nullable T data);

    /**
     * 指定某一页数据
     */
    Map<String, String> page(@Nullable T data, int page);

    /**
     * 是否有下一页
     */
    boolean hasMore(@Nullable T data);
}
