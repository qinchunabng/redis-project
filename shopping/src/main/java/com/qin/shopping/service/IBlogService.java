package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.entity.Blog;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:42.
 */
public interface IBlogService extends IService<Blog> {

    Page<Blog> queryHotBlog(Integer current);

    Blog queryById(Long id);
}
