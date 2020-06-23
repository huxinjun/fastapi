package org.pulp.main;

import android.os.Bundle;
import android.view.View;

import org.pulp.fastapi.ApiClient;
import org.pulp.fastapi.util.ULog;

import androidx.appcompat.app.AppCompatActivity;
import cn.aichang.blackbeauty.base.net.api.TestAPI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void initUrlConfig() {
        ApiClient.getApi(TestAPI.class)
                .getConfig()
                .success(data -> ULog.out("success:" + data))
                .unreachable((error, url) -> ULog.out("unreachable:" + url))
                .faild(error -> ULog.out("faild:" + error))
                .toastError();
    }


    public void test(View view) {
        initUrlConfig();
    }
}
