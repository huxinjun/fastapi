package org.pulp.main.page;

import android.support.annotation.Nullable;

import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.util.Log;

import java.util.Map;

public class BadCondition implements PageCondition<Object> {
    @Override
    public Map<String, String> nextPage(@Nullable Object data) {
        return null;
    }

    @Override
    public Map<String, String> prePage(@Nullable Object data) {
        return null;
    }

    @Override
    public Map<String, String> page(@Nullable Object data, int page) {
        return null;
    }

    @Override
    public boolean hasMore(@Nullable Object data, MoreType moreType) {
        Log.out("hasMore.data=" + data);
        return false;
    }
}
