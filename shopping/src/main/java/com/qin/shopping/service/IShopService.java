package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.entity.Shop;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/20 22:03.
 */
public interface IShopService extends IService<Shop> {

    Shop queryById(Long id);

    boolean update(Shop shop);
}
