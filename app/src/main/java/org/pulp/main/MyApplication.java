package org.pulp.main;

import android.app.Application;

import org.pulp.fastapi.ApiClient;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(ApiSetting.get(this));

    }


}
