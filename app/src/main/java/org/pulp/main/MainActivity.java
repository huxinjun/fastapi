package org.pulp.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.pulp.fastapi.API;
import org.pulp.fastapi.extension.SimpleListObservable;
import org.pulp.fastapi.extension.SimpleObservable;
import org.pulp.fastapi.model.Str;
import org.pulp.main.model.ListModel;
import org.pulp.main.model.TestModel;
import org.pulp.main.model.UrlKey;

import java.net.URL;

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
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                }, 0);
    }


    public void testMultiPathAnno(View view) {
        clear();
        output("requesting...\n");
        API.get(this, TestAPI.class)
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
        URL result = API.get(this, TestAPI.class).getStaticUrlConvertPath("abc");
        output(((TextView) view).getText() + "\n");
        output(result);
    }

    public void testStaticUrl_NoConvertPath(View view) {
        clear();
        URL result = API.get(this, TestAPI.class).getStaticUrlNoConvertPath("efg");
        output(((TextView) view).getText() + "\n");
        output(result);
    }

    public void testGetData_ConvertPath(View view) {
        clear();
        SimpleObservable<TestModel> data = API.get(this, TestAPI.class).getDataConvertPath();
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
        SimpleObservable<Str> data = API.get(this, TestAPI.class).getDataNoConvertPath();
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
        }).lookTimeUsed("hahaha");
    }

    public void testGetData_CacheUseAll(View view) {
        clear();
        SimpleObservable<Str> data = API.get(this, TestAPI.class).getDataCacheUseAll();
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

    //分页-------------------------------------------------------------------------------------------
    SimpleListObservable<ListModel> data = API.get(this, TestAPI.class).getListData("method test param");

    public void testGetListDataPre(View view) {
        data.prePage().success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------\n");
        }).lookTimeUsed("list time use---");

    }

    public void testGetListDataFirst(View view) {
        data.reset().success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------\n");
        }).lookTimeUsed("list time use---");

    }

    public void testGetListDataNext(View view) {

        data.nextPage().success(str -> {
            output("on success callback:\n");
            output(((TextView) view).getText() + "\n");
            output(str + "\n");
            output("-----------------");
        }).faild(error -> {
            output("on error callback:\n");
            output(((TextView) view).getText() + "\n");
            output(error.getMsg() + "\n");
            output("-----------------\n");
        }).lookTimeUsed("list time use---");

    }

    //重置
    public void testGetListData_Reset(View view) {
        clear();
        data.reset();
    }


    //测试子线程发起请求
    public void testGetData_NotMainThread(View view) {
        new Thread(() -> {
            SimpleObservable<TestModel> data = API.get(this, TestAPI.class).getDataConvertPath();
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
        }).start();

    }


    //--------------------------------------------------
    public void clear() {
        tv_content.setText("");
    }

    public void output(@Nullable Object txt) {
        tv_content.append(txt == null ? "null" : txt.toString());
    }

}
