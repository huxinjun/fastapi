package org.pulp.fastapi.model;

public class _String implements IModel{
    String content;

    public _String(String content) {
        this.content = content;
    }

    @Override
    public void onSetIsCache(boolean isCache) {

    }

    @Override
    public void onSetUrlPath(String urlPath) {

    }

    @Override
    public String onBeforeParse(String json) {
        return null;
    }
}
