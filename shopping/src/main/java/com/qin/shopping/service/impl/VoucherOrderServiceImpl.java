package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.entity.SeckillVoucher;
import com.qin.shopping.entity.VoucherOrder;
import com.qin.shopping.exception.BusinessException;
import com.qin.shopping.mapper.VoucherOrderMapper;
import com.qin.shopping.service.ISeckillVoucherService;
import com.qin.shopping.service.IVoucherOrderService;
import com.qin.shopping.utils.RedisIdWorker;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 20:54.
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Override
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

        //锁加在外部，如果放在方法内部，由于锁先释放，事务后提交的
        //可能会导致锁释放后，其他线程获取到锁的时候，事务未提交，导致重复下单
        synchronized (userId.toString().intern()) {
            //防止事务失效
            IVoucherOrderService voucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return voucherOrderService.createVoucherOrder(voucherId, userId);
        }
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
}
