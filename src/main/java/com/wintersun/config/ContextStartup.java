package com.wintersun.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wintersun.entity.Category;
import com.wintersun.service.CategoryService;
import com.wintersun.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

@Component
public class ContextStartup implements ApplicationRunner, ServletContextAware {

    @Autowired
    CategoryService categoryService;

    ServletContext servletContext;

    @Autowired
    PostService postService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<Category> categories = categoryService.list(new QueryWrapper<Category>()
        .eq("status",0));
        servletContext.setAttribute("categories",categories);

        postService.initWeekRank();

    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
