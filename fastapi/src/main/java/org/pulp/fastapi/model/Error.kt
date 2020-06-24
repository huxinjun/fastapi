package org.pulp.fastapi.model


/**
 * 全局的错误对象
 * 会被包装成此对象通过SimpleObservable.Faild:void onFaild(Error error)传递
 */
class Error {


    companion object {
        //不是错误.支持静态URL不发出真实请求,在请求chain中抛出此异常
        const val STATIC_URL_TRICK = -10000

        const val ERR_CRASH = -9999
        const val ERR_NO_MORE_DATA = -9998
        const val ERR_NO_NET = -9997
        const val ERR_PARSE_CLASS = -9996
        const val ERR_PARSE_BEAN = -9995
        const val ERR_PARSE_CUSTOM = -9994
        const val ERR_ALL_URLS_INVALID = -9994


        var SYMBOL: String = "ERROR_SYMBOL"

        fun err2str(error: Error): String {
            return "$SYMBOL${error.code}$SYMBOL${error.msg}$SYMBOL${error.tag}"
        }

        fun str2err(str: String): Error? {
            val error = Error()
            val arr: List<String> = str.split(SYMBOL)
            if (arr.size == 4) {
                error.code = arr.get(1).toInt()
                error.msg = arr.get(2)
                error.tag = arr.get(3)
            }
            return error
        }
    }


    //错误码
    var code: Int = 0

    //错误描述
    var msg: String? = ""

    //自定义
    var tag: Any? = null


    override fun toString(): String {
        return "Error(code=$code, msg=$msg, tag=$tag)"
    }


}

