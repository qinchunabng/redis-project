package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.entity.Voucher;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 19:48.
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 添加秒杀优惠券
     * @param voucher
     */
    void addSeckillVoucher(Voucher voucher);
}
