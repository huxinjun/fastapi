package org.pulp.fastapi.model;


public class Error {
    public static String SYMBOL = "ERROR_SYMBOL";

    public static final int STATIC_URL_TRICK = -10000;
    public static final int ERR_CRASH = -9999;
    public static final int ERR_NO_MORE_DATA = -9998;
    public static final int ERR_NO_NET = -9997;
    public static final int ERR_PARSE_CLASS = -9996;
    public static final int ERR_PARSE_ERROR = -9995;
    public static final int ERR_PARSE_BEAN = -9994;
    public static final int ERR_PARSE_CUSTOM = -9993;
    public static final int ERR_ALL_URLS_INVALID = -9992;

    private int code;
    private String msg;
    private Object tag;

    public static String err2str(Error error) {
        return SYMBOL + error.getCode() + SYMBOL + error.getMsg() + SYMBOL + error.getTag();
    }

    public static Error str2err(String str) {
        Error error = new Error();
        String[] split = str.split(SYMBOL);
        if (split.length == 4) {
            error.code = Integer.parseInt(split[1]);
            error.msg = split[2];
            error.tag = split[3];
        }
        return error;
    }


    public final int getCode() {
        return this.code;
    }

    public final void setCode(int var1) {
        this.code = var1;
    }

    public final String getMsg() {
        return this.msg;
    }

    public final void setMsg(String var1) {
        this.msg = var1;
    }

    public final Object getTag() {
        return this.tag;
    }

    public final void setTag(Object var1) {
        this.tag = var1;
    }

}
