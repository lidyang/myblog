package com.wintersun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.Comment;
import com.wintersun.mapper.CommentMapper;
import com.wintersun.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wintersun.vo.CommentVo;
import com.wintersun.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public IPage<CommentVo> paging(Page page, Long postId, Long userId, String orderField) {

        return commentMapper.selectComments(page,new QueryWrapper<Comment>()
        .eq(postId != null,"post_id",postId)
        .eq(userId != null,"user_id",userId)
        .orderByDesc(orderField));
    }
}
