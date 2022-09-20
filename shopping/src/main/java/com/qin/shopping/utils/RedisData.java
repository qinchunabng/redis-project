package com.qin.shopping.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 封装redis逻辑过期数据
 *
 * @author qcb
 * @date 2022/09/19 22:44.
 */
@Data
public class RedisData {

    private LocalDateTime expiryTime;

    private Object data;
}
