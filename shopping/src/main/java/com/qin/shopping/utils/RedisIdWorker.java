package com.qin.shopping.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * ID生成器
 *
 * @author qcb
 * @date 2022/09/17 18:48.
 */
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;

    /**
     * 序列号位数
     */
    private static final int SEQUENCE_BIT_COUNT = 32;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 生成ID
     * ID分为三个部分
     * 0 - 0000000 00000000 00000000 - 00000000 00000000 00000000
     * 符号位 - 时间戳（31bit） - 序列号（32bit）
     * @param keyPrefix
     * @return
     */
    public long nextId(String keyPrefix){
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        //2.生成序列号
        //2.1 获取当前的日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //2.2 自增长
        long count = redisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //3.拼接返回
        return timestamp << SEQUENCE_BIT_COUNT | count;
    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2022,1,1,0,0,0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println("Second = " + second);
    }
}
