package org.pulp.main.page;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.pulp.fastapi.i.PageCondition;
import org.pulp.fastapi.util.Log;

import java.util.Map;

public class BadCondition implements PageCondition<Object> {
    @Override
    public Map<String, String> nextPage(@NonNull @Nullable Object data) {
        return null;
    }

    @Override
    public Map<String, String> prePage(@NonNull @Nullable Object data) {
        return null;
    }

    @Override
    public Map<String, String> page(@NonNull @Nullable Object data, int page) {
        return null;
    }

    @Override
    public boolean hasMore(@NonNull Object data, @NonNull MoreType moreType) {
        Log.out("hasMore.data=" + data);
        return false;
    }
}
