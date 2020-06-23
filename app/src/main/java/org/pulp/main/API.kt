package cn.aichang.blackbeauty.base.net.api

import org.pulp.fastapi.anno.DataParser
import org.pulp.fastapi.anno.MultiPath
import org.pulp.fastapi.extension.SequenceObservable
import org.pulp.fastapi.extension.SimpleObservable
import org.pulp.fastapi.model._String
import org.pulp.main.UrlKey
import org.pulp.main.UrlKeyParser
import retrofit2.http.GET
import retrofit2.http.Query

interface TestAPI {

    @DataParser(UrlKeyParser::class)
    @MultiPath("https://alapi.mengliaoba.cn/apiv5/getconfig.php",
            "http://123.56.221.219:8280/apiv5/getconfig.php",
            "http://api.5aikan.cn/apiv5/getconfig.php",
            "http://api.mengface.cn/apiv5/getconfig.php",
            "http://api.mengliaoba.cn/apiv5/getconfig.php")
    fun getConfig(): SequenceObservable<UrlKey>


    @GET(UrlKey.COMMON_FLASHSCREEN)
    fun getStartPic(@Query("tag") tag: String): SimpleObservable<_String>
}

