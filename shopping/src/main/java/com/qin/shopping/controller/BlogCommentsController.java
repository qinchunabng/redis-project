package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.entity.BlogComments;
import com.qin.shopping.service.IBlogCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:54.
 */
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    @Autowired
    private IBlogCommentsService blogCommentsService;

    @PostMapping
    public Result saveComment(@RequestBody BlogComments blogComments){
        blogCommentsService.save(blogComments);
        return Result.ok(blogComments.getId());
    }
}
