package com.wintersun.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wintersun.vo.PostVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
public interface PostService extends IService<Post> {

//    1.分页信息、2.分类、3.用户、4.置顶、5.精选、6.排序
    IPage<PostVo> paging(Page page, Long categoryId, Long userId, Integer level, Boolean recommend, String orderField);

    PostVo selectOnePost(QueryWrapper<Post> wrapper);

    void initWeekRank();

    void incrComentCountAndUnionForWeekRank(Post post, Boolean isIncr);

    void putViewCount(PostVo vo);


}
