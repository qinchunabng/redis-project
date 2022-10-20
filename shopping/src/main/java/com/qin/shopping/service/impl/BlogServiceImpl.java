package com.qin.shopping.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.constants.RedisConstants;
import com.qin.shopping.constants.SystemConstants;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.entity.Blog;
import com.qin.shopping.entity.Follow;
import com.qin.shopping.entity.User;
import com.qin.shopping.exception.BusinessException;
import com.qin.shopping.mapper.BlogMapper;
import com.qin.shopping.service.IBlogService;
import com.qin.shopping.service.IFollowService;
import com.qin.shopping.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IFollowService followService;

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

    @Override
    public boolean like(Long id, Long userId) {
        //1.判断用户是已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = redisTemplate.opsForZSet().score(key, userId);
        if(score == null){
            //2.如果未点赞，可以点赞
            //2.1数据库点赞+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            //2.2保存用户到redis的set集合
            if(isSuccess){
                redisTemplate.opsForZSet().add(key, userId, System.currentTimeMillis());
            }
            return isSuccess;
        }

        //3.如果已点赞，取消点赞
        //3.1数据库点赞-1
        boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
        //3.2把用户从redis的set集合中移除
        if(isSuccess){
            redisTemplate.opsForZSet().remove(key, userId);
        }
        return isSuccess;
    }

    @Override
    public List<UserDTO> queryBlogLikes(Long id) {
        //1.查询top5的点赞用户zrange key 0 4
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Set<Long> tops = redisTemplate.opsForZSet().range(key, 0, 4);
        if(tops == null || tops.isEmpty()){
            return Collections.emptyList();
        }
        //2.解析用户id
        List<Long> userIdList = new ArrayList<>(tops);
        //3.根据用户id查询用户信息
        String idStr = StrUtil.join(",", userIdList);
        List<User> users = userService.query()
                .in("id", userIdList)
                .last("ORDER BY FIELD (id," + idStr + ")")
                .list();
        return users.stream()
                .map(user -> new UserDTO(user.getId(), user.getNickName(), user.getIcon()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Long saveBlog(Blog blog) {
        //1.保存博客
        boolean success = save(blog);
        if(!success){
            //1.1失败直接返回
            throw new BusinessException("保存博客失败");
        }
        //2.查询所有的当前用的所有的分析
        List<Follow> followList = followService.lambdaQuery()
                .eq(Follow::getFollowUserId, blog.getUserId())
                .list();
        //3.推送博客给所有的粉丝
        if(followList == null || followList.isEmpty()){
            return blog.getId();
        }
        for(Follow follow : followList){
            //3.1获取粉丝的ID
            Long userId = follow.getUserId();
            //3.2推送
            redisTemplate.opsForZSet().add(RedisConstants.FEED_KEY + userId, blog.getId(), System.currentTimeMillis());
        }
        return blog.getId();
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
