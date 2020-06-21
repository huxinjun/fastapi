package org.pulp.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.pulp.fastapi.TEST;

import cn.aichang.blackbeauty.base.net.api.CommonAPI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TEST t=new TEST();
        t.getT();
        int _=0;
    }


    public static void initUrlConfig() {
        getApi(CommonAPI.class)
                .getConfig()
                .success(data -> ApiClient.GlobalUrlkey = data)
                .unreachable((observable, error) -> observable.nextUrl());
//                .toastError();
    }
}
