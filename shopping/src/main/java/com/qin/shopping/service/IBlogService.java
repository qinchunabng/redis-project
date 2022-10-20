package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.entity.Blog;

import java.util.List;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:42.
 */
public interface IBlogService extends IService<Blog> {

    Page<Blog> queryHotBlog(Integer current);

    Blog queryById(Long id);

    boolean like(Long id, Long userId);

    List<UserDTO> queryBlogLikes(Long id);

    Long saveBlog(Blog blog);
}
