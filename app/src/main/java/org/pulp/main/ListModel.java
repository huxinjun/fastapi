package org.pulp.main;

import org.pulp.fastapi.model.IModel;

public class ListModel implements IModel {

    private int page_next;
    private int page_previous;
    private int page;
    private int page_count;

    public int getPage_next() {
        return page_next;
    }

    public int getPage_previous() {
        return page_previous;
    }

    public int getPage() {
        return page;
    }

    @Override
    public void setCache(boolean isCache) {

    }

    public int getPage_count() {
        return page_count;
    }
}
