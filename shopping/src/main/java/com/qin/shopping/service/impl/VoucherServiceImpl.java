package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.entity.SeckillVoucher;
import com.qin.shopping.entity.Voucher;
import com.qin.shopping.mapper.VoucherMapper;
import com.qin.shopping.service.ISeckillVoucherService;
import com.qin.shopping.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
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
    }
}
