package com.qin.shopping.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

/**
 * 可重入redis锁
 *
 * @author qcb
 * @date 2022/10/20 20:49.
 */
public class ReentrantRedisLock implements ILock {

    private static final String KEY_PREFIX = "reentrant_lock:";

    private static final String ID_PREFIX = UUID.randomUUID().toString().replace("-", "") + "-";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    private static final DefaultRedisScript<Long> LOCK_SCRIPT;

    static {
        LOCK_SCRIPT = new DefaultRedisScript<>();
        LOCK_SCRIPT.setLocation(new ClassPathResource("reentrantlock.lua"));
        LOCK_SCRIPT.setResultType(Long.class);
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("reentrantunlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    private String name;

    private RedisTemplate redisTemplate;

    private long timeoutSec;

    public ReentrantRedisLock(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取当前线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        this.timeoutSec = timeoutSec;
        Long result = (Long) redisTemplate.execute(LOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), threadId, timeoutSec);
        return result != null && result == 1;
    }

    @Override
    public void unlock() {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), threadId, timeoutSec);
    }
}
