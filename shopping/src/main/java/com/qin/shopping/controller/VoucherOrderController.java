package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.service.IVoucherOrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;

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

    @PostMapping("/seckill")
    public Result seckillVoucher(@RequestParam("id")Long voucherId, @RequestParam("userId") Long userId){
        voucherOrderService.seckillVoucher(voucherId, userId);
        return Result.ok();
    }

    public static void main(String[] args) throws Exception{
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("F:\\Workspace\\redis\\users.txt"), StandardCharsets.UTF_8);
        for (int i = 1; i <= 1000; i++) {
            writer.write(i + "\n");
        }
        writer.flush();
        writer.close();
    }
}
