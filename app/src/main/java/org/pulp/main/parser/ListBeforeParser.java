package org.pulp.main.parser;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.i.InterpreterParseBefore;

public class ListBeforeParser implements InterpreterParseBefore {
    @Override
    public String onBeforeParse(@NonNull String json) {
        JSONObject jsonObject = null;
        try {
            String ret = null;
            jsonObject = new JSONObject(json);
            //result提取剥离
            if (jsonObject.has("result")) {
                Object result = jsonObject.opt("result");
                if (jsonObject.length() == 1 && result instanceof JSONObject)
                    ret = result.toString();
            }
            if (!TextUtils.isEmpty(ret))
                return ret;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
