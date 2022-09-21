package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.entity.Shop;
import com.qin.shopping.service.IShopService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/20 22:05.
 */
@Api("商店API")
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private IShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id){
        return Result.ok(shopService.queryById(id));
    }

    @PutMapping
    public Result updateShop(@RequestBody Shop shop){
        boolean success = shopService.update(shop);
        return Result.ok(success);
    }
}
