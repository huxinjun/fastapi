
package org.pulp.fastapi.util;

import android.util.Log;

public class ULog {

    static public void out(Object message) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        Log.i("xinjun", ste.getFileName() + ": Line " + ste.getLineNumber()
                + "---result------------------------->" + (message == null ? "" : message.toString()));
    }
}
