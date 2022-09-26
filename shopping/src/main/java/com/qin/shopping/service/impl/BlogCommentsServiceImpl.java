package com.qin.shopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qin.shopping.entity.BlogComments;
import com.qin.shopping.mapper.BlogCommentsMapper;
import com.qin.shopping.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/26 22:53.
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {
}
