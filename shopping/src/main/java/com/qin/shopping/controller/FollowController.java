package com.qin.shopping.controller;

import com.qin.shopping.dto.Result;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/28 22:10.
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow, @RequestParam("userId") Long userId){
        followService.follow(followUserId, userId, isFollow);
        return Result.ok();
    }


    @GetMapping("/or/not/{id}")
    public Result follow(@PathVariable("id") Long followUserId, @RequestParam("userId") Long userId){
        return Result.ok(followService.isFollow(followUserId, userId));
    }

    @GetMapping("/common/{id}")
    public Result commonFollow(@PathVariable("id") Long followUserId, @RequestParam("userId") Long userId){
        List<UserDTO> list = followService.commonFollow(followUserId, userId);
        return Result.ok(list);
    }
}
