package org.pulp.fastapi.model;


import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String toString() {
        return "Str{" +
                "isCache=" + isCache +
                ", content='" + content + '\'' +
                '}';
    }
}
