package org.pulp.fastapi.model;


public class Error {
    public static String SYMBOL = "ERROR_SYMBOL";

    public static final int STATIC_URL_TRICK = -10000;

    public static final int ERR_CRASH = -9000;
    public static final int ERR_ALL_URLS_INVALID = -9001;
    public static final int ERR_NO_NET = -9002;

    public static final int ERR_NO_MORE_DATA = -8000;
    public static final int ERR_NO_PREVIOUS_DATA = -8001;
    public static final int ERR_NO_PAGE_DATA = -8002;
    public static final int ERR_PAGE_CONDITION_TYPE_BAD = -8003;

    public static final int ERR_PARSE_ERROR = -7000;
    public static final int ERR_PARSE_BEAN = -7001;
    public static final int ERR_PARSE_CUSTOM = -7002;


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
