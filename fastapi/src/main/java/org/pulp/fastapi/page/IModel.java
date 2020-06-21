package org.pulp.fastapi.page;

import java.util.Map;

public interface IModel {


    void onSetIsCache(boolean isCache);
    void onSetUrlPath(String urlPath);
    String onBeforeParse(String json);
}
