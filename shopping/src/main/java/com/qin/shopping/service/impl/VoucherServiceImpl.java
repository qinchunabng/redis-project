package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.constants.RedisConstants;
import com.qin.shopping.entity.SeckillVoucher;
import com.qin.shopping.entity.Voucher;
import com.qin.shopping.mapper.VoucherMapper;
import com.qin.shopping.service.ISeckillVoucherService;
import com.qin.shopping.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 19:50.
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加秒杀优惠券
     * @param voucher
     */
    @Transactional
    @Override
    public void addSeckillVoucher(Voucher voucher) {
        //保存优惠券
        save(voucher);
        //保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        //保存秒杀库存到redis中
        redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock());
    }
}
