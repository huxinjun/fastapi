package org.pulp.fastapi.page;

import java.util.Map;

public interface IListModel extends IModel{

    boolean hasMore();

    Map<String, String> onPrePage();

    Map<String, String> onNextPage();
}
