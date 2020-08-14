package org.pulp.fastapi.model;


import android.text.TextUtils;

import org.pulp.fastapi.Bridge;

public class Error {
    public static String SYMBOL = "ERROR_SYMBOL";


    /**
     * error code
     * Created by xinjun on 2020/8/14 9:05 PM
     */
    public enum Code {

        STATIC_URL_TRICK(-10000),

        CRASH(-9000),
        ALL_URLS_INVALID(-9001),
        NO_NET(-9002),

        NO_MORE_DATA(-8000),
        NO_PREVIOUS_DATA(-8001),
        NO_PAGE_DATA(-8002),
        PAGE_CONDITION_TYPE_BAD(-8003),

        PARSE_ERROR(-7000),
        PARSE_BEAN(-7001),
        PARSE_CUSTOM(-7002);


        public int code;

        Code(int code) {
            this.code = code;
        }
    }

    private int code;
    private String msg;
    private boolean isCustomer;//是否解析的是自定义的错误

    public static String err2str(Error error) {
        return SYMBOL + error.getCode() + SYMBOL + error.getMsg() + SYMBOL + error.isCustomer;
    }

    public static Error str2err(String str) {
        Error error = new Error();
        String[] split = str.split(SYMBOL);
        if (split.length == 4) {
            error.code = Integer.parseInt(split[1]);
            error.msg = split[2];
            error.isCustomer = Boolean.parseBoolean(split[3]);
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

    public final boolean isCustomer() {
        return isCustomer;
    }

    public final void setCustomer(boolean customer) {
        isCustomer = customer;
    }

}
