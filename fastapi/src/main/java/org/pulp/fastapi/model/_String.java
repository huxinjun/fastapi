package org.pulp.fastapi.model;

public class _String implements IModel {
    private String content;

    public _String(String content) {
        this.content = content;
    }

    @Override
    public void setCache(boolean isCache) {

    }
}
