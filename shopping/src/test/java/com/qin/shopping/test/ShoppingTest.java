package com.qin.shopping.test;

import com.qin.shopping.utils.ILock;
import com.qin.shopping.utils.RedisIdWorker;
import com.qin.shopping.utils.ReentrantRedisLock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 19:07.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShoppingTest {

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    private static ILock lock;

    @BeforeAll
    public void init(){
        lock = new ReentrantRedisLock("test", redisTemplate);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(500);

    @Test
    public void testIdWorker() throws Exception{
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    public void testLock() throws Exception{
        CountDownLatch latch = new CountDownLatch(10);
//        int i = 0;
        for(int n=0;n<100;n++){
            int finalN = n;
            executorService.submit(() -> {
                outLock();
//                System.out.println("n=" + finalN);
                latch.countDown();
            });
        }
        latch.await();
        TimeUnit.SECONDS.sleep(10);
//        System.in.read();
//        System.out.println("i=" + i);
//        outLock();
    }

    public void outLock(){
        boolean locked = lock.tryLock(100);
        if(locked){
            try {
                System.out.println(Thread.currentThread().getName() + ": outLock success");
//                i++;
                innerLock();
            }finally {
                lock.unlock();
            }
        }else {
//            System.out.println(Thread.currentThread().getName() + ": outLock failed");
        }
    }

    public void innerLock(){
        boolean locked = lock.tryLock(100);
        if(locked){
            try {
                System.out.println(Thread.currentThread().getName() + ": innerLock success");
//                i--;
            }finally {
                lock.unlock();
            }
        }else{
//            System.out.println(Thread.currentThread().getName() + ": innerLock failed");
        }
    }
}
