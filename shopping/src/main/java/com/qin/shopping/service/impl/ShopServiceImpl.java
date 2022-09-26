package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.constants.RedisConstants;
import com.qin.shopping.entity.Shop;
import com.qin.shopping.exception.BusinessException;
import com.qin.shopping.mapper.ShopMapper;
import com.qin.shopping.service.IShopService;
import com.qin.shopping.utils.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 商店业务逻辑
 *
 * @author qcb
 * @date 2022/09/20 22:04.
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheClient cacheClient;

    @Override
    public Shop queryById(Long id){
        Shop shop = cacheClient.queryWithMutex(RedisConstants.CACHE_SHOP_KEY, id,
                Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if(shop == null){
            throw new BusinessException("店铺不存在");
        }

        return shop;
    }

    @Transactional
    @Override
    public boolean update(Shop shop) {
        Long id = shop.getId();
        if(id == null){
            throw new BusinessException("店铺ID不能为空");
        }
        //更新数据库
        boolean success = updateById(shop);
        if(success){
            redisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        }
        return success;
    }
}
