package cn.aichang.blackbeauty.base.net.api

import io.reactivex.Observable
import org.pulp.fastapi.anno.MULTI_PATH
import org.pulp.fastapi.extension.SequenceObservable
import retrofit2.http.*

/**
 * 声明API的地方
 *
 * 支持返回类型为Observable<?>或SimpleObservable<?>或SimpleListObservable<?>
 * 返回类型为SimpleObservable<?>或SimpleListObservable<?>时可:
 *      1.方法注解直接配置UrlKey中的key即可确定请求地址
 *          1):@HTTP(method = "POST", path = UrlKey.HOT_TODAY_SELECTED)
 *          2):@HTTP(method = "GET", path = UrlKey.HOT_TODAY_SELECTED)
 *          3):@GET(UrlKey.HOT_TODAY_SELECTED)
 *          4):@POST(UrlKey.HOT_TODAY_SELECTED)
 *      2.简化回调方式
 *      3.分页(参数不需传页码):需要返回类型为或SimpleListObservable<?>
 *      4.Server Error解析
 *      5.result剥离(请求结果是一个只包含result的JsonObject,result类型为JsonObject)
 *
 * 返回类型为Observable<?>时需要使用@Url配置请求Url
 * Created by xinjun on 2019/12/11.
 */
interface CommonAPI {

    @GET("USE_CONFIG_ANNO_URL")
    @MULTI_PATH("https://alapi.mengliaoba.cn/apiv5/getconfig.php",
            "http://123.56.221.219:8280/apiv5/getconfig.php",
            "http://api.5aikan.cn/apiv5/getconfig.php",
            "http://api.mengface.cn/apiv5/getconfig.php",
            "http://api.mengliaoba.cn/apiv5/getconfig.php")
    fun getConfig(): SequenceObservable<UrlKey>

    @GET(UrlKey.COMMON_FLASHSCREEN)
    fun getStartPic(@Query("tag") tag: String): SimpleObservable<StringModel>

    //webp url
    @GET(UrlKey.ALBUM_FC_WEBP)
    fun getWebpUrl(@Query("uid") uid: String?, @Query("fcid") fcid: String?): AichangUrl

    //翻唱封面
    @GET(UrlKey.ALBUM_FC_COVER)
    fun getCoverUrl(@Query("uid") uid: String?,
                    @Query("fcid") fcid: String?,
                    @Query("artist") artist: String?,
                    @Query("show_user") show_user: String?): AichangUrl

    //分享使用的翻唱封面
    @GET(UrlKey.ALBUM_FC_COVER_DATA)
    fun getCoverDataUrl(@Query("uid") uid: String?,
                        @Query("fcid") fcid: String?,
                        @Query("artist") artist: String?,
                        @Query("show_user") show_user: String?): AichangUrl

    //歌曲地址
    @GET(UrlKey.URL_SONG)
    @PARAM("type", "fcurl")
    fun getSongUrl(@Query("fcid") fcid: String?): AichangUrl

    //歌曲封面
    @GET(UrlKey.URL_SONGPIC)
    @PARAM("size", "m")
    fun getSongPicUrl(@Query("artist") artist: String?): AichangUrl

    //歌词地址
    @GET(UrlKey.URL_LYRIC)
    fun getLyricUrl(@Query("bzid") bzid: String?): AichangUrl

}

interface TestAPI {

    //Observable测试,必须传url参数
    @POST
    fun getJingXuanNormal(@Url url: String): Observable<TopicList>

    //SimpleListObservable测试,有nextPage,prePage,hasMore,pageCondition等关于分页的方法
    @HTTP(method = "POST", path = UrlKey.HOT_TODAY_SELECTED, hasBody = true)
    @Headers("Cache-Control:public,max-age=60")
    fun getJingXuanList(): SimpleListObservable<TopicList>

    //SimpleObservable测试,有success,faild,refresh方法
    @GET(UrlKey.HOT_TODAY_SELECTED)
    fun getJingXuan(): SimpleObservable<TopicList>

    //测试微博流获取,使用PAGE注解配置一个PageCondition类来自定义分页,使用PARAM注解声明固定不变的url参数
    @PAGE(WeiboListPageCondition::class)
    @POST(UrlKey.USER_MYFRIEND_TOPICS_RECOMMENT)
    @PARAMS(PARAM("test1", "hu"), PARAM("test2", "xin"))
    @PARAM("test3", "jun")
    fun getWeiboliu(): SimpleListObservable<TopicList>
}

