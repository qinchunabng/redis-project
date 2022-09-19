package com.qin.shopping.utils;

/**
 * 分布式锁接口
 *
 * @author qcb
 * @date 2022/09/19 22:16.
 */
public interface ILock {

    boolean tryLock(long timeoutSec);

    void unlock();
}
