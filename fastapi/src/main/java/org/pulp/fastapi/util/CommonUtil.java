package org.pulp.fastapi.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.pulp.fastapi.model.Error;

public class CommonUtil {


    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().isConnected();
    }

    public static void throwError(int code, String msg) {
        Error error = new Error();
        error.setCode(code);
        error.setMsg(msg);
        throwError(error);
    }

    public static void throwError(Error error) {
        throw new RuntimeException(Error.Companion.err2str(error));
    }
}
