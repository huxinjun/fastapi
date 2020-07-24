package org.pulp.main.model;

import org.pulp.fastapi.model.IListModel;
import org.pulp.fastapi.model.IModel;

public class ListModel implements IListModel {

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

    @Override
    public String toString() {
        return "ListModel{" +
                "page_next=" + page_next +
                ", page_previous=" + page_previous +
                ", page=" + page +
                ", page_count=" + page_count +
                '}';
    }

    @Override
    public int onGetPageIndex() {
        return page;
    }
}
