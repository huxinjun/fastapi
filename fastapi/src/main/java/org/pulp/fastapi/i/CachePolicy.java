package org.pulp.fastapi.i;

/**
 * 支持的缓存策略
 * 默认的缓存策略是无网络使用缓存
 * 可使用注解Header配置在API上,如:
 * @Headers(CachePolicy.ONLY_NETWORK)
 * Created by xinjun on 2020/1/15 17:08
 */
public interface CachePolicy {

    /**
     * 无缓存
     * 使用网络
     * 缓存:不使用也不存储
     */
    String NONE = "Cache-Control:no-cache,no-store";
    /**
     * 默认,无网使用缓存
     * 请求网络数据后就会缓存起来
     */
    String WHEN_FAILD = "Cache-Control:public";

    /**
     * 只使用缓存
     */
    String ONLY_CAHCE = "Cache-Control:public,only-if-cached,max-stale=" + Integer.MAX_VALUE;

    /**
     * 只使用网络
     */
    String ONLY_NETWORK = "Cache-Control:public,no-cache";

    /**
     * 如果有缓存先使用缓存,再请求网络
     * 一次网络请求将会有2次回调(没有缓存时1次)
     */
    String USE_ALL = "Cache-Control:all";


}
