package cn.aichang.blackbeauty.base.net.api

import org.pulp.fastapi.anno.*
import org.pulp.fastapi.i.CachePolicy.*
import org.pulp.fastapi.extension.SequenceObservable
import org.pulp.fastapi.extension.SimpleObservable
import org.pulp.fastapi.model.Str
import org.pulp.main.*
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL

//@OnCustomParse(TestClassParserAnno::class)
//@OnErrorParse(TestClassParserAnno::class)
//@OnBeforeParse(TestClassParserAnno::class)
@OnAfterParse(TestClassParserAnno::class)
interface TestAPI {

    @GET("getConfig")
    @OnCustomParse(UrlKeyParser::class)
    @MultiPath(
            "https://alapi.mengliaoba.cn/apiv5/getconfig.php",
            "http://123.56.221.219:8280/apiv5/getconfig.php",
            "http://api.5aikan.cn/apiv5/getconfig.php",
            "http://api.mengface.cn/apiv5/getconfig.php",
            "http://api.mengliaoba.cn/apiv5/getconfig.php"
    )
    fun getConfig(): SequenceObservable<UrlKey>


    @GET(UrlKey.COMMON_FLASHSCREEN)
    fun getStaticUrlConvertPath(@Query("tag") tag: String): URL

    @BaseUrl("http://www.xzbenben.cn")
    @GET("/AccountBook/account")
    fun getStaticUrlNoConvertPath(@Query("tag") tag: String): URL

    //    @OnBeforeParse(TestMethodParserAnno::class)
//    @OnCustomParse(TestMethodParserAnno::class)
//    @OnErrorParse(TestMethodParserAnno::class)
    @OnAfterParse(TestMethodParserAnno::class)
    @GET(UrlKey.COMMON_FLASHSCREEN)
    fun getDataConvertPath(): SimpleObservable<TestModel>

    @GET("https://alapi.mengliaoba.cn/apiv5/common/flashscreen.php")
    fun getDataNoConvertPath(): SimpleObservable<Str>


    @Cache(ONLY_CAHCE)
    @GET(UrlKey.COMMON_FLASHSCREEN)
    fun getDataCacheUseAll(): SimpleObservable<Str>
}

