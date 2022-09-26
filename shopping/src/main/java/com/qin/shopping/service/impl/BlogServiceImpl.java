package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.constants.SystemConstants;
import com.qin.shopping.entity.Blog;
import com.qin.shopping.entity.User;
import com.qin.shopping.mapper.BlogMapper;
import com.qin.shopping.service.IBlogService;
import com.qin.shopping.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:44.
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IUserService userService;

    @Override
    public Page<Blog> queryHotBlog(Integer current) {
        //分页查询
        Page<Blog> page = query()
                .orderByAsc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        //获取当前数据页
        List<Blog> records = page.getRecords();
        records.forEach(this::queryBlogUser);
        return page;
    }

    @Override
    public Blog queryById(Long id){
        Blog blog = getById(id);
        queryBlogUser(blog);
        return blog;
    }

    private void queryBlogUser(Blog blog){
        if(blog == null){
            return;
        }
        User user = userService.getById(blog.getUserId());
        if(user != null){
            blog.setIcon(user.getIcon());
            blog.setName(user.getNickName());
        }
    }
}
