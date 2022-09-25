package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.entity.VoucherOrder;
import com.qin.shopping.exception.BusinessException;
import com.qin.shopping.mapper.VoucherOrderMapper;
import com.qin.shopping.service.ISeckillVoucherService;
import com.qin.shopping.service.IVoucherOrderService;
import com.qin.shopping.utils.ILock;
import com.qin.shopping.utils.RedisIdWorker;
import com.qin.shopping.utils.SimpleRedisLock;
import com.qin.shopping.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 20:54.
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    private static final BlockingQueue<VoucherOrder> ORDER_TASK_QUEUE = new ArrayBlockingQueue<>(1024 * 1024);

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private IVoucherOrderService voucherOrderService;

    private static final String QUEUE_NAME = "stream.orders";
    private static final String GROUP_NAME = "g1";
    private static final String CONSUMER_NAME = "c1";

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    public IVoucherOrderService getVoucherOrderService(){
        if(voucherOrderService == null){
            voucherOrderService = SpringContextUtils.getBean(IVoucherOrderService.class);
        }
        return voucherOrderService;
    }

    @PostConstruct
    public void init(){
        SECKILL_ORDER_EXECUTOR.submit(() -> {
            while(true){
                try {
                    //1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
                            Consumer.from(GROUP_NAME, CONSUMER_NAME),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(QUEUE_NAME, ReadOffset.lastConsumed())
                    );
                    //2.判断消息是否获取成功
                    if(list == null || list.size() == 0){
                        //2.1获取失败，说明没有消息，继续下一次循环
                        continue;
                    }
                    //3.解析消息中的订单消息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = new VoucherOrder();
                    voucherOrder.setId(values.get("id") != null ? (Long) values.get("id") : null);
                    voucherOrder.setVoucherId(values.get("voucherId") != null ? ((Integer) values.get("voucherId")).longValue() : null);
                    voucherOrder.setUserId(values.get("userId") != null ? ((Integer) values.get("userId")).longValue() : null);
                    //4.获取成功，可以下单
                    handleVoucherOrder(voucherOrder);
                    //5.ACK确认 SACK stream.orders g1 id
                    redisTemplate.opsForStream().acknowledge(QUEUE_NAME, GROUP_NAME, record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        });
    }

    private void handlePendingList(){
        while(true){
            try {
                //1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(QUEUE_NAME, ReadOffset.from("0"))
                );
                //2.判断消息是否获取成功
                if(list == null || list.size() == 0){
                    //2.1获取失败，说明pending-list没有消息，结束循环
                    break;
                }
                //3.解析消息中的订单消息
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> values = record.getValue();
                VoucherOrder voucherOrder = new VoucherOrder();
                voucherOrder.setId(values.get("id") != null ? (Long) values.get("id") : null);
                voucherOrder.setVoucherId(values.get("voucherId") != null ? ((Integer) values.get("voucherId")).longValue() : null);
                voucherOrder.setUserId(values.get("userId") != null ? ((Integer) values.get("userId")).longValue() : null);
                //4.获取成功，可以下单
                handleVoucherOrder(voucherOrder);
                //5.ACK确认 SACK stream.orders g1 id
                redisTemplate.opsForStream().acknowledge(QUEUE_NAME, GROUP_NAME, record.getId());
            } catch (Exception e) {
                log.error("处理pending-list订单异常", e);
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

//    @PostConstruct
//    public void init(){
//        SECKILL_ORDER_EXECUTOR.submit(() -> {
//           while(true){
//               try {
//                   //1.获取队列中的订单信息
//                   VoucherOrder voucherOrder = ORDER_TASK_QUEUE.take();
//                   //2.创建订单
//                   handleVoucherOrder(voucherOrder);
//               } catch (Exception e) {
//                   log.error(e.getMessage(), e);
//               }
//           }
//        });
//    }

    private void handleVoucherOrder(VoucherOrder voucherOrder){
        //更换为分布式锁
        ILock lock = new SimpleRedisLock("order:" + voucherOrder.getUserId(), redisTemplate);
        //获取锁
        boolean isLock = lock.tryLock(5);
        if(!isLock){
            throw new BusinessException("不允许重复下单");
        }
        try {
            getVoucherOrderService().createVoucherOrder(voucherOrder.getVoucherId(), voucherOrder.getUserId());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public Long seckillVoucher(Long voucherId, Long userId) {
        //1.执行lua脚本
        //获取订单ID
        long orderId = redisIdWorker.nextId("order");
        Long result = (Long) redisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId, userId, orderId);
        //2.判断是否有执行资格
        //2.1.不为0，没有下单资格
        if(result != 0){
            throw new BusinessException(result == 1 ? "库存不足" : "不能重复下单");
        }
        //2.2.为0，有购买资格，把下单信息保存到阻塞队列中
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        //2.3.将订单放到阻塞队列中
//        voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
//        try {
//            boolean addResult = ORDER_TASK_QUEUE.offer(voucherOrder, 10, TimeUnit.SECONDS);
//            if(!addResult){
//                throw new BusinessException("下单超时");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        //3.返回订单ID
        return orderId;
    }

    @Transactional
    public Long createVoucherOrder(Long voucherId, Long userId) {
        //5.一人一单
        Long count = query().eq("user_id", userId)
                .eq("voucher_id", voucherId)
                .count();
        if(count != null && count > 0){
            throw new BusinessException("用户已经购买过一次");
        }

        //6、扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if(!success){
            throw new BusinessException("库存不足");
        }
        //7、创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //7.1订单id
        voucherOrder.setId(redisIdWorker.nextId("order"));
        //7.2用户id
        voucherOrder.setUserId(userId);
        //7.3代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        //8、返回订单ID
        return voucherOrder.getId();
    }

    /*@Override
    public Long seckillVoucher(Long voucherId, Long userId) {
        //1、查询优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if(seckillVoucher == null){
            throw new BusinessException("找不到对应的优惠券");
        }
        //2、判断秒杀是否开始
        if(seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())){
            throw new BusinessException("秒杀尚未开始");
        }
        //3、判断秒杀是否结束
        if(seckillVoucher.getEndTime().isBefore(LocalDateTime.now())){
            throw new BusinessException("秒杀已经结束");
        }
        //4、判断库存是否充足
        if(seckillVoucher.getStock() < 1){
            throw new BusinessException("库存不足");
        }

        //更换为分布式锁
        ILock lock = new SimpleRedisLock("order:" + userId, redisTemplate);
        //获取锁
        boolean isLock = lock.tryLock(5);
        if(!isLock){
            throw new BusinessException("不允许重复下单");
        }
        try {
            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return voucherOrderService.createVoucherOrder(voucherId, userId);
        }finally {
            lock.unlock();
        }
        //锁加在外部，如果放在方法内部，由于锁先释放，事务后提交的
        //可能会导致锁释放后，其他线程获取到锁的时候，事务未提交，导致重复下单
//        synchronized (userId.toString().intern()) {
//            //防止事务失效
//            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
//            return voucherOrderService.createVoucherOrder(voucherId, userId);
//        }
    }*/
}
