package org.pulp.main.model;

import org.pulp.fastapi.model.IModel;

public class TestModel implements IModel {

    public int tag;
    public boolean isCache;
    public String testFrom;
    @Override
    public void setCache(boolean isCache) {
        this.isCache = isCache;
    }

    @Override
    public String toString() {
        return "TestModel{" +
                "tag=" + tag +
                ", isCache=" + isCache +
                ", testFrom='" + testFrom + '\'' +
                '}';
    }
}
