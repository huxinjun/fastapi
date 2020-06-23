package org.pulp.main;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.i.Parser;
import org.pulp.fastapi.model.Error;

public class CommonParser implements Parser {
    @Override
    public String onBeforeParse(String json) {
        JSONObject jsonObject = null;
        String jsonStr = null;
        try {
            jsonObject = new JSONObject(json);
            //result提取剥离
            if (jsonObject.has("result")) {
                Object result = jsonObject.opt("result");
                if (jsonObject.length() == 1 && result instanceof JSONObject)
                    jsonStr = result.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonStr;
    }

    @Override
    public Error onParseError(String json) throws Exception {
        return parseError(json);
    }

    @Override
    public Object onCustomParse(String json) {
        return null;
    }


    private Error parseError(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        if (obj.has("error") && obj.has("code")) {
            Error error = new Error();
            error.setCode(obj.optInt("code", 0));
            error.setMsg(obj.optString("errmsg", ""));
            if (TextUtils.isEmpty(error.getMsg()))
                error.setMsg(obj.optString("result"));
            return error;
        }
        return null;
    }
}
