package com.wintersun;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wintersun.entity.Post;
import com.wintersun.service.PostService;
import com.wintersun.vo.CommentVo;
import com.wintersun.vo.PostVo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
class MyblogApplicationTests {

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    PostService postService;


    @Test
    void contextLoads() {
        System.out.println("method: detail " );

        int id = 1;
        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>().eq("p.id", id));
        Assert.notNull(postVo,"文章已被删除");
//        postService.putViewCount(postVo);

        System.out.println(postVo);

    }

}
