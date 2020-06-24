package org.pulp.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.pulp.fastapi.ApiClient;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.model.Str;
import org.pulp.fastapi.util.ULog;

import java.net.URL;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import cn.aichang.blackbeauty.base.net.api.TestAPI;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_content = findViewById(R.id.tv_content);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                0);
    }


    public void testMultiPathAnno(View view) {
        clear();
        output("requesting...\n");
        ApiClient.getApi(TestAPI.class)
                .getConfig()
                .success(data -> {
                    output("on success callback:\n");
                    output(((TextView) view).getText() + "\n");
                    output(UrlKey.downloadUrls + "\n");
                    output("-----------------");

                })
                .unreachable((error, url) -> {
                    output("on unreachable callback:\n");
                    output(((TextView) view).getText() + "\n");
                    output(error.getMsg() + "\n");
                    output("url=" + url + "\n");
                    output("-----------------");
                })
                .faild(error -> {
                    output("on error callback:\n");
                    output(((TextView) view).getText() + "\n");
                    output(error.getMsg() + "\n");
                    output("-----------------");
                });
    }


    public void testStaticUrl_ConvertPath(View view) {
        clear();
        URL result = ApiClient.getApi(TestAPI.class).getStaticUrlConvertPath("abc");
        output(((TextView) view).getText() + "\n");
        output(result);
    }

    public void testStaticUrl_NoConvertPath(View view) {
        clear();
        URL result = ApiClient.getApi(TestAPI.class).getStaticUrlNoConvertPath("efg");
        output(((TextView) view).getText() + "\n");
        output(result);
    }

    public void testGetData_ConvertPath(View view) {
        clear();
        SimpleObservable<Str> data = ApiClient.getApi(TestAPI.class).getDataConvertPath();
        data.success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------");
        });
    }

    public void testGetData_NoConvertPath(View view) {
        clear();
        SimpleObservable<Str> data = ApiClient.getApi(TestAPI.class).getDataNoConvertPath();
        data.success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------");
        });
    }

    public void testGetData_CacheUseAll(View view) {
        clear();
        SimpleObservable<Str> data = ApiClient.getApi(TestAPI.class).getDataCacheUseAll();
        data.success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------");
        });
    }

    public void clear() {
        tv_content.setText("");
    }

    public void output(@Nullable Object txt) {
        tv_content.append(txt == null ? "null" : txt.toString());
    }
}
