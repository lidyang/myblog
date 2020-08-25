package com.wintersun.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wintersun.entity.Post;
import com.wintersun.mapper.PostMapper;
import com.wintersun.service.PostService;
import com.wintersun.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ViewCountSyncTask {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    PostService postService;


    @Scheduled(cron = "0/10 * * * * *")
    public void task(){

        System.out.println("=====================开始同步ViewCount=====================");

        Set<String> keys = redisTemplate.keys("rank:post:*");
        List<String> ids = new ArrayList<>();
        for (String key : keys) {
            if(redisUtil.hHasKey(key,"post:viewCount")){
                ids.add(key.substring("rank:post:".length()));
            }
        }

        if(ids.isEmpty()) return;

        List<Post> posts = postService.list(new QueryWrapper<Post>().in("id", ids));
        if(posts.isEmpty()) return;
        posts.stream().forEach(post -> {
            Integer viewCount = (Integer) redisUtil.hget("rank:post:" + post.getId(), "post:viewCount");
            post.setViewCount(viewCount);
        });

        boolean isOk = postService.updateBatchById(posts);

        if(isOk){
            ids.stream().forEach(id -> {
                redisUtil.hdel("rank:post"+id,"post:viewCount");
            });
            System.out.println("=====================同步成功=====================");
        }

    }



}
