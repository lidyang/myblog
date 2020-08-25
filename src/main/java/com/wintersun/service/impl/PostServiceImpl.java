package com.wintersun.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.Post;
import com.wintersun.mapper.PostMapper;
import com.wintersun.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wintersun.util.RedisUtil;
import com.wintersun.vo.PostVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public IPage<PostVo> paging(Page page, Long categoryId, Long userId, Integer level, Boolean recommend, String orderField) {


        if(level == null ) {
            level = -1;
        }
        QueryWrapper wrapper = new QueryWrapper<Post>()
                .eq(categoryId != null,"category_id",categoryId)
                .eq(userId != null,"user_id",userId)
                .eq(level == 0,"level",0)
                .eq(level > 0,"level",1)
                .eq(recommend != null,"recommend",recommend)
                .orderByDesc(orderField != null,orderField);
        IPage<PostVo> postVoIPage = postMapper.selectPosts(page, wrapper);

        return postVoIPage;
    }

    @Override
    public PostVo selectOnePost(QueryWrapper<Post> wrapper) {
        return postMapper.selectOnePost(wrapper);
    }

    @Override
    public void initWeekRank() {

//        获取7天内发表的文章

        List<Post> posts = this.list(new QueryWrapper<Post>()
                .gt("created", DateUtil.offsetDay(new Date(), -7))
                .select("id,title,user_id,comment_count,view_count,created"));

//        初始化文章的总评论数

        for (Post post:posts){
            String key = "day:rank:" + DateUtil.format(post.getCreated(), DatePattern.PURE_DATE_FORMAT);

            redisUtil.zSet(key,post.getId(),post.getCommentCount());

            long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
            long expireTime = (7 - between) * 24 * 60 * 60;
            redisUtil.expire(key,expireTime);
            //            缓存文章基本信息（id，标题，评论数量，作者）
            this.hashCachePostIdAndTitle(post,expireTime);
        }



//        并集操作
        this.unionAndStoreLast7DaysForWeekRank();

    }

    /**
     * 合并最近一周每天的评论数量
     */
    private void unionAndStoreLast7DaysForWeekRank() {
        String key = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        String destKey = "week:rank";

        List<String> otherKeys = new ArrayList();

        for(int i = -6; i<0; i++ ){
            String temp = "day:rank:" + DateUtil.format(DateUtil.offsetDay(new Date(), i), DatePattern.PURE_DATE_FORMAT);
            otherKeys.add(temp);
        }

        redisUtil.zUnionAndStore(key,otherKeys,destKey);
    }

    private void hashCachePostIdAndTitle(Post post, long expireTime) {

        String key = "rank:post:" + post.getId();
        boolean hasKey = redisUtil.hasKey(key);

        if(!hasKey){
            redisUtil.hset(key,"post:id",post.getId(),expireTime);
            redisUtil.hset(key,"post:title",post.getTitle(),expireTime);
            redisUtil.hset(key,"post:commentCount",post.getCommentCount(),expireTime);
            redisUtil.hset(key,"post:viewCount",post.getViewCount(),expireTime);
        }
    }


    /**
     * 增加评论数量并更新到缓存中
     *
     */
    @Override
    public void incrComentCountAndUnionForWeekRank(Post post, Boolean isIncr) {

        String currentKey = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);

        redisUtil.zIncrementScore(currentKey,post.getId(),isIncr?1:-1);

        long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
        long expireTime = (7 - between) * 24 * 60 * 60;

//        缓存文章基本信息（id，标题，评论数量，作者）
        this.hashCachePostIdAndTitle(post,expireTime);

//        合并最近一周每天的评论数量
        this.unionAndStoreLast7DaysForWeekRank();


    }

    @Override
    public void putViewCount(PostVo vo) {
        String postKey = "rank:post:" + vo.getId();

        Integer viewCount = (Integer) redisUtil.hget(postKey, "post:viewCount");

        if(viewCount != null){
            vo.setViewCount(viewCount + 1);
        } else{
            vo.setViewCount(vo.getViewCount() + 1);
        }

        redisUtil.hset(postKey,"post:viewCount",vo.getViewCount());


    }

}
