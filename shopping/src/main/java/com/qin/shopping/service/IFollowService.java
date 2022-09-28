package com.qin.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.entity.Follow;

import java.util.List;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/28 22:18.
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注用户
     * @param followUserId 关注的用户ID
     * @param userId 当前用户ID
     * @param isFollow 关注还是取消关注
     */
    void follow(Long followUserId, Long userId, Boolean isFollow);

    /**
     * 是否关注
     * @param followUserId 关注的用户ID
     * @param userId 当前用户ID
     * @return
     */
    boolean isFollow(Long followUserId, Long userId);

    /**
     * 共同关注
     * @param userId1
     * @param userId2
     * @return
     */
    List<UserDTO> commonFollow(Long userId1, Long userId2);
}
