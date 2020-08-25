package com.wintersun.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.common.templates.DirectiveHandler;
import com.wintersun.common.templates.TemplateDirective;
import com.wintersun.service.PostService;
import com.wintersun.util.PageInfoUtil;
import com.wintersun.util.RedisUtil;
import com.wintersun.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HotsTemplate extends TemplateDirective {


    @Autowired
    RedisUtil redisUtil;


    @Override
    public String getName() {

        return "hots";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {

//        缓存中每周统计排名key
        String weekKey = "week:rank";

//        通过key获取数据
        Set<ZSetOperations.TypedTuple> typedTuples = redisUtil.getZSetRank(weekKey, 0, 6);

        List<Map> hotPosts = new ArrayList<>();
        for (ZSetOperations.TypedTuple typedTuple : typedTuples) {
            Map<String,Object> map = new HashMap<>();

            Object value = typedTuple.getValue();//post id
            String postKey = "rank:post:" + value;

            map.put("id",value);
            map.put("title",redisUtil.hget(postKey,"post:title"));
            map.put("commentCount",typedTuple.getScore());

            hotPosts.add(map);

        }

        handler.put(RESULTS,hotPosts).render();
    }



//    private String getName(){
//        return "post";
//    }


}
