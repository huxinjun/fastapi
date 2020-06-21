package org.pulp.fastapi.page;


import androidx.annotation.Nullable;

import org.pulp.fastapi.extension.SimpleListObservable;
import org.pulp.fastapi.util.ULog;

import java.util.HashMap;
import java.util.Map;


/**
 * 默认分页
 * Created by xinjun on 2019/12/9 11:20
 */
public class DeffaultPageCondition<T extends IListModel> implements SimpleListObservable.PageCondition<T> {

    public static final String NO_MORE_DATA = "NO_MORE_DATA";

    private static final int PAGE_COUNT = 20;

    private static final HashMap<String, String> deffaultParamMap = new HashMap<String, String>() {
        {
            put("page", String.valueOf(1));
            put("count", String.valueOf(PAGE_COUNT));
            put("limit", String.valueOf(PAGE_COUNT));
        }
    };

    @Override
    public Map<String, String> nextPage(@Nullable T data) {
        if (data == null)
            return deffaultParamMap;
        int page_next = data.getPage_next();
        return page(data, page_next);
    }

    @Override
    public Map<String, String> prePage(@Nullable T data) {
        if (data == null)
            return deffaultParamMap;
        int page_previous = data.getPage_previous();
        return page(data, page_previous);
    }

    @Override
    public Map<String, String> page(@Nullable T data, int go) {
        ULog.out("go=" + go);
        if (data == null)
            return deffaultParamMap;
        int page = data.getPage();
        int page_next = data.getPage_next();
        int page_count = data.getPage_count();
        ULog.out("page=" + page);
        ULog.out("page_next=" + page_next);
        ULog.out("page_count=" + page_count);

        if (go > 0) {
            if (go == page_next) {
                if (page == page_count) {
                    return new HashMap<String, String>() {
                        {
                            put(NO_MORE_DATA, NO_MORE_DATA);
                        }
                    };
                }
            }

            if (page > page_count) {
                return new HashMap<String, String>() {
                    {
                        put(NO_MORE_DATA, NO_MORE_DATA);
                    }
                };
            }
        }

        return new HashMap<String, String>() {
            {
                put("page", String.valueOf(go));
                put("count", String.valueOf(PAGE_COUNT));
            }
        };
    }
}
