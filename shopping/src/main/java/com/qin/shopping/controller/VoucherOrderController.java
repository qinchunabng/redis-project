package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.service.IVoucherOrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 20:50.
 */
@Api("代金券订单API")
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @PostMapping("/seckill/{id}/{userId}")
    public Result seckillVoucher(@PathVariable("id")Long voucherId, @PathVariable("userId") Long userId){
        voucherOrderService.seckillVoucher(voucherId, userId);
        return Result.ok();
    }
}
