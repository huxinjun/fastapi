package org.pulp.main.api;

import android.app.Application;

import org.pulp.fastapi.API;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        API.init(ApiSetting.get(this));

    }


}
