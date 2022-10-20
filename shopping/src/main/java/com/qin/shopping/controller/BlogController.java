package com.qin.shopping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qin.shopping.dto.Result;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.entity.Blog;
import com.qin.shopping.service.IBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:41.
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private IBlogService blogService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog){
        return Result.ok(blogService.saveBlog(blog));
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current){
        Page<Blog> page = blogService.queryHotBlog(current);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long id){
        Blog blog = blogService.queryById(id);
        return Result.ok(blog);
    }

    @PutMapping("/like/{id}")
    public Result like(@PathVariable Long id, @RequestParam Long userId){
        return Result.ok(blogService.like(id, userId));
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable Long id){
        List<UserDTO> users = blogService.queryBlogLikes(id);
        return Result.ok(users);
    }
}
