package org.pulp.fastapi.i;

/**
 * 支持的缓存策略
 * 默认的缓存策略是无网络使用缓存
 * 可使用注解Cache配置在API上,如:
 * <p>
 * Created by xinjun on 2020/1/15 17:08
 */
public enum CachePolicy {


    /**
     * 无缓存
     * 使用网络
     * 缓存:不使用也不存储
     */
    NONE("Cache-Control:no-cache,no-store"),

    /**
     * 默认,无网使用缓存
     * 请求网络数据后就会缓存起来
     */
    WHEN_FAILD("Cache-Control:public"),

    /**
     * 只使用缓存
     */
    ONLY_CAHCE("Cache-Control:public,only-if-cached,max-stale=" + Integer.MAX_VALUE),

    /**
     * 只使用网络
     */
    ONLY_NETWORK("Cache-Control:public,no-cache"),

    /**
     * 如果有缓存先使用缓存,再请求网络
     * 一次网络请求将会有2次回调(没有缓存时1次)
     */
    USE_ALL("Cache-Control:all");

    private String value;

    CachePolicy(String value) {
        this.value = value;
    }

    public static CachePolicy parse(String value) {
        CachePolicy[] values = CachePolicy.values();
        for (CachePolicy type : values) {
            if (type.value.equalsIgnoreCase(value))
                return type;
        }
        return WHEN_FAILD;
    }

    public String getValue() {
        return value;
    }
}
