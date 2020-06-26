package org.pulp.main;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.util.ULog;

import java.util.Iterator;


public class UrlKeyParser implements InterpreterParserCustom<UrlKey> {

    @Override
    public UrlKey onCustomParse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json).optJSONObject("content");
            Iterator iterator1 = jsonObject.keys();
            String k1;
            JSONObject v1;
            while (iterator1.hasNext()) {
                k1 = (String) iterator1.next();
                v1 = jsonObject.optJSONObject(k1);
                if (v1 != null) {
                    Iterator iterator2 = v1.keys();
                    String k2;
                    String v2;
                    while (iterator2.hasNext()) {
                        k2 = (String) iterator2.next();
                        v2 = v1.optString(k2, "");
                        if (!TextUtils.isEmpty(k2) && !TextUtils.isEmpty(v2)) {
                            UrlKey.downloadUrls.put(k1 + "_" + k2, v2);
                        }
                    }

                }
            }
            ULog.out("parseUrlKey.downloadUrls =" + UrlKey.downloadUrls);
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
