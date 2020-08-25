package com.wintersun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wintersun.vo.CommentVo;
import com.wintersun.vo.PostVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
public interface CommentService extends IService<Comment> {

    IPage<CommentVo> paging(Page page, Long postId, Long userId, String orderField);

}
