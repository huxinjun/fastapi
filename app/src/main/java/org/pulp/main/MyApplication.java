package org.pulp.main;

import android.app.Application;

import org.pulp.fastapi.ApiClient;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {


    Map<String, String> commonParams = new HashMap<String, String>() {
        {
            put("base_market", "test");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();


        ApiClient.init(this,
                getExternalCacheDir().getPath(),
                path -> path,
                new CommonParser(),
                commonParams);

    }


}
