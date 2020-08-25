package com.wintersun.config;

import com.jagregory.shiro.freemarker.ShiroTags;
import com.wintersun.template.HotsTemplate;
import com.wintersun.template.PostsTemplate;
import com.wintersun.template.TimeAgoMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.sql.Time;

@Configuration
public class FreemarkerConfig {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Autowired
    private PostsTemplate postsTemplate;

    @Autowired
    private TimeAgoMethod timeAgoMethod;

    @Autowired
    private HotsTemplate hotsTemplate;

    @PostConstruct
    public void setUp(){
        configuration.setSharedVariable("timeAgo", timeAgoMethod);
        configuration.setSharedVariable("posts", postsTemplate);
        configuration.setSharedVariable("hots", hotsTemplate);
        configuration.setSharedVariable("shiro", new ShiroTags());

    }



}
