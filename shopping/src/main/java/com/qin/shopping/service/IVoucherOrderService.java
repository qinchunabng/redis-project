package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.entity.VoucherOrder;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 20:53.
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Long seckillVoucher(Long voucherId, Long userId);

    Long createVoucherOrder(Long voucherId, Long userId);
}
