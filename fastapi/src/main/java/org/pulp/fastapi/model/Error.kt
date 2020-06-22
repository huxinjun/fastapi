package org.pulp.fastapi.model


/**
 * 全局的错误对象
 * 请求网络中途出现的错误:
 * 1.程序错误(代码抛出的异常,code=-9999)
 * 2.网络问题(Socket问题)
 * 3.服务器错误(服务器定义的错误码,在ContextError中查询)
 * 会被包装成此对象通过SimpleObservable.Faild:void onFaild(Error error)传递
 */
class Error {


    companion object {
        const val ERR_APP = -9999
        const val ERR_NO_MORE_DATA = -9998
        const val ERR_NO_NET = -9997

        var SYMBOL: String = "ERROR_SYMBOL"

        fun err2str(error: Error): String {
            return "$SYMBOL${error.code}$SYMBOL${error.status}$SYMBOL${error.msg}$SYMBOL${error.desc}"
        }

        fun str2err(str: String): Error? {
            val error = Error()
            val arr: List<String> = str.split(SYMBOL)
            if (arr.size == 5) {
                error.code = arr.get(1).toInt()
                error.status = arr.get(2)
                error.msg = arr.get(3)
                error.desc = arr.get(4)
            }
            return error
        }
    }


    //错误码
    var code: Int = 0

    //状态描述
    var status: String? = ""

    //错误信息
    var msg: String? = ""

    //字典描述
    var desc: String? = ""


    override fun toString(): String {
        return "Error(code=$code, status='$status', msg='$msg', desc='$desc')"
    }


}

