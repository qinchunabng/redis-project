package com.qin.shopping.constants;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/20 22:51.
 */
public interface RedisConstants {

    String CACHE_KEY = "cache:";

    String SHOP_LOCK_KEY = "shop";

    /**
     * 商店信息缓存key
     */
    String CACHE_SHOP_KEY = CACHE_KEY + "shop:";

    /**
     * 商品缓存过期时间
     */
    long CACHE_SHOP_TTL = 30L;

    /**
     * NULL缓存过期时间
     */
    long CACHE_NULL_TTL = 1L;

    /**
     * 秒杀库存key
     */
    String SECKILL_STOCK_KEY = "seckill:stock:";
}
