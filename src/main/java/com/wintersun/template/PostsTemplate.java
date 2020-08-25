package com.wintersun.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.common.templates.DirectiveHandler;
import com.wintersun.common.templates.TemplateDirective;
import com.wintersun.service.PostService;
import com.wintersun.util.PageInfoUtil;
import com.wintersun.vo.PostVo;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostsTemplate extends TemplateDirective {

    @Autowired
    private PostService postService;


    @Override
    public String getName() {

        return "posts";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {

        Integer level = handler.getInteger("level");
        Integer pn = handler.getInteger("pn", PageInfoUtil.PAGE_NUM);
        Integer size = handler.getInteger("size", PageInfoUtil.TOP_LEVEL_PAGE_SIZE);
        Long categoryId = handler.getLong("categoryId");

        IPage<PostVo> page = postService.paging(new Page(pn, size), categoryId, null, level, null, "created");
        handler.put(RESULTS,page).render();
    }



//    private String getName(){
//        return "post";
//    }


}
