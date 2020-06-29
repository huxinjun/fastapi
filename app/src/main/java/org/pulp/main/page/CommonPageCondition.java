package org.pulp.main.page;


import android.support.annotation.Nullable;

import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.util.Log;
import org.pulp.main.model.ListModel;

import java.util.HashMap;
import java.util.Map;


/**
 * 默认分页
 * Created by xinjun on 2019/12/9 11:20
 */
public class CommonPageCondition<T extends ListModel> implements PageCondition<T> {

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
        Log.out("go=" + go);
        if (data == null)
            return deffaultParamMap;
        int page = data.getPage();
        int page_next = data.getPage_next();
        int page_count = data.getPage_count();
        Log.out("page=" + page);
        Log.out("page_next=" + page_next);
        Log.out("page_count=" + page_count);

        return new HashMap<String, String>() {
            {
                put("page", String.valueOf(go));
                put("count", String.valueOf(PAGE_COUNT));
            }
        };
    }

    @Override
    public boolean hasMore(@Nullable T data) {
        if (data == null)
            return true;
//        return data.getPage_next() > data.getPage();
        return data.getPage_next() < 5;
    }
}
