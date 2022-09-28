package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.constants.RedisConstants;
import com.qin.shopping.dto.UserDTO;
import com.qin.shopping.entity.Follow;
import com.qin.shopping.mapper.FollowMapper;
import com.qin.shopping.service.IFollowService;
import com.qin.shopping.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/28 22:18.
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IUserService userService;

    /**
     * 关注用户
     * @param followUserId 关注的用户ID
     * @param userId 当前用户ID
     * @param isFollow 关注还是取消关注
     */
    @Transactional
    @Override
    public void follow(Long followUserId, Long userId, Boolean isFollow) {
        if(isFollow) {
            //1.关注
            boolean isFollowed = isFollow(followUserId, userId);
            if(!isFollowed){
                Follow follow = new Follow();
                follow.setFollowUserId(followUserId);
                follow.setUserId(userId);
                boolean success = save(follow);
                if(success){
                    //1.1关注成功，添加redis set集合中
                    redisTemplate.opsForSet().add(RedisConstants.FOLLOW_USER_KEY + userId, followUserId);
                }
            }
        }else{
            //2.取消关注
            boolean success = remove(
                    new QueryWrapper<Follow>()
                            .lambda()
                            .eq(Follow::getUserId, userId)
                            .eq(Follow::getFollowUserId, followUserId)
            );
            if(success){
                //1.1取消关注成功，从redis set集合中移除
                redisTemplate.opsForSet().remove(RedisConstants.FOLLOW_USER_KEY + userId, followUserId);
            }
        }
    }

    /**
     * 是否关注
     * @param followUserId 关注的用户ID
     * @param userId 当前用户ID
     * @return
     */
    @Override
    public boolean isFollow(Long followUserId, Long userId) {
        Long count = lambdaQuery().eq(Follow::getFollowUserId, followUserId)
                .eq(Follow::getUserId, userId)
                .count();
        return count > 0;
    }

    @Override
    public List<UserDTO> commonFollow(Long userId1, Long userId2) {
        //1.查询两个用户共同关注的用户的交集
        Set<Long> commonUserIdSet = redisTemplate.opsForSet().intersect(RedisConstants.FOLLOW_USER_KEY + userId1, RedisConstants.FOLLOW_USER_KEY + userId2);
        if(commonUserIdSet == null || commonUserIdSet.isEmpty()){
            return Collections.emptyList();
        }
        //2.从数据查询共同关注用户的信息
        return userService.listByIds(commonUserIdSet)
                .stream()
                .map(user -> new UserDTO(user.getId(), user.getNickName(), user.getIcon()))
                .collect(Collectors.toList());
    }
}
