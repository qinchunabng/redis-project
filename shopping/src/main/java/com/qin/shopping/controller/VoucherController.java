package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.entity.Voucher;
import com.qin.shopping.service.IVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 19:47.
 */
@Api("优惠券API")
@RestController
@RequestMapping(value ="/voucher")
public class VoucherController {

    @Autowired
    private IVoucherService voucherService;

    /**
     * 新增秒杀优惠券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping("/seckill")
    @ApiOperation("新增秒杀优惠券")
    public Result addSeckillVoucher(@RequestBody Voucher voucher){
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }
}
