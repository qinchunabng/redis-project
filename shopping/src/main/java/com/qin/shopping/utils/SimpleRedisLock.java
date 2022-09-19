package com.qin.shopping.utils;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/19 22:20.
 */
public class SimpleRedisLock implements ILock{

    private static final String KEY_PREFIX = "lock:";

    private String name;

    private RedisTemplate redisTemplate;

    public SimpleRedisLock(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取当前线程标识
        long threadId = Thread.currentThread().getId();
        //获取锁
        Boolean success = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        redisTemplate.delete(KEY_PREFIX + name);
    }
}
