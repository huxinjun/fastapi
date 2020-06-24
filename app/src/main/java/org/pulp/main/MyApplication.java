package org.pulp.main;

import android.Manifest;
import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;

import org.pulp.fastapi.ApiClient;
import org.pulp.fastapi.util.ULog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.core.app.ActivityCompat;

public class MyApplication extends Application {


    Map<String, String> commonParams = new HashMap<String, String>() {
        {
            put("base_market", "test");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        String cacheDir = new File(Environment.getExternalStorageDirectory().getPath(), "fastapi").getPath();

        ApiClient.init(this,
                cacheDir,
                path -> {
                    String url = null;
                    if (UrlKey.downloadUrls != null)
                        url = UrlKey.downloadUrls.get(path);
                    if (TextUtils.isEmpty(url))
                        url = UrlKey.defaultUrls.get(path);
                    return url;
                },
                new CommonParser(),
                commonParams);

    }


}
