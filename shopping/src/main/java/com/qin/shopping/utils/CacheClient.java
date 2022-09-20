package com.qin.shopping.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/19 22:42.
 */
@Slf4j
@Component
public class CacheClient {

    private static final long CACHE_NULL_TTL = 1L;

    private final RedisTemplate redisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit){
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        //写入redis
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpiryTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        //写入redis
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }


    /**
     * 缓存穿透
     * @param keyPrefix key前缀
     * @param id
     * @param type 返回值类型class
     * @param <R> 返回值泛型
     * @param <ID> ID泛型
     * @return
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbCallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        //1.查询缓存
        String json = (String) redisTemplate.opsForValue().get(key);
        //2.判断缓存是否存在
        if(StrUtil.isNotEmpty(json)){
            return JSONUtil.toBean(json, type);
        }
        //判断命中的值是否是空值
        if(json != null){
            return null;
        }
        if(dbCallback == null){
            return null;
        }
        //4.不存在，根据id查询数据库
        R r = dbCallback.apply(id);
        //5.不存在，返回错误
        if(r == null){
            //将空值写入redis
            redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //6.存在，写入redis
        this.set(key, r, time, unit);
        return r;
    }

    /**
     * 缓存逻辑过期查询
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param type 缓存类型
     * @param dbCallback 数据库查询操作
     * @param time 过期时间
     * @param unit 过期时间单位
     * @param <R> 返回值泛型
     * @param <ID> 查询值泛型
     * @return
     */
    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbCallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        //1.查询缓存
        String json = (String) redisTemplate.opsForValue().get(key);
        //2.判断缓存是否存在
        if(StrUtil.isEmpty(json)){
            return null;
        }
        //3.命中把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpiryTime();
        //4.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //4.1未过期直接返回
            return r;
        }
        //4.2未过期，需要重建缓存
        //5重建缓存
        //5.1获取互斥锁
        ILock lock = new SimpleRedisLock(id.toString(), redisTemplate);
        boolean isSuccess = lock.tryLock(5);
        //5.2判断锁是否获取成功
        if(isSuccess){
            //5.3获取锁成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //查询数据库
                    R r1 = dbCallback.apply(id);
                    //写入redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }finally {
                    lock.unlock();
                }
            });
        }
        //返回查询信息
        return r;
    }
}
