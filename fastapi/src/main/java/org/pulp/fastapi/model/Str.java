package org.pulp.fastapi.model;


import androidx.annotation.NonNull;

public class Str implements IModel {

    private boolean isCache;
    private String content;

    public Str(String content) {
        this.content = content;
    }

    @Override
    public void setCache(boolean isCache) {
        this.isCache = isCache;
    }

    public boolean isCache() {
        return isCache;
    }

    @NonNull
    @Override
    public String toString() {
        return content;
    }
}
