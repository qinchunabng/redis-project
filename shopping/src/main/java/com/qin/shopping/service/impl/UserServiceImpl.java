package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.entity.User;
import com.qin.shopping.mapper.UserMapper;
import com.qin.shopping.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 23:21.
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
