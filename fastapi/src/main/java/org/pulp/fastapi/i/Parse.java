package org.pulp.fastapi.i;

import org.json.JSONObject;

public interface Parse {
    String onBeforeParse(String json);
    Error onParseError(JSONObject obj);
}
