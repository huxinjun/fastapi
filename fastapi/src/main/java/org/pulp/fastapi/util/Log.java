
package org.pulp.fastapi.util;

public class Log {

    private static final boolean debug = true;

    static public void out(Object message) {
        if (!debug)
            return;
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        android.util.Log.i("xinjun", ste.getFileName() + ": Line " + ste.getLineNumber()
                + "---result------------------------->" + (message == null ? "" : message.toString()));
    }
}
