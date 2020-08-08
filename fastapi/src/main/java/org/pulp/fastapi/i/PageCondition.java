package org.pulp.fastapi.i;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;


/**
 * 自定义分页回调
 * Created by xinjun on 2019/12/9 11:13
 */
public interface PageCondition<T> {
    enum MoreType {
        NextPage,
        PrePage
    }

    /**
     * 下一页数据
     *
     * @param data 当前数据
     * @return 要添加在请求中的参数
     */
    @Nullable Map<String, String> nextPage(@NonNull T data);

    /**
     * 上一页数据
     *
     * @param data 当前数据
     * @return 要添加在请求中的参数
     */
    @Nullable Map<String, String> prePage(@NonNull T data);

    /**
     * 指定某一页数据
     */
    @Nullable Map<String, String> page(@NonNull T data, int page);

    /**
     * 是否有下一页
     */
    boolean hasMore(@NonNull T data,@NonNull MoreType moreType);
}
