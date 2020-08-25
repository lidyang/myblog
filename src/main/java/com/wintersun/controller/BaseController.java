package com.wintersun.controller;


import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.UserMessage;
import com.wintersun.service.*;
import com.wintersun.shiro.AccountProfile;
import com.wintersun.util.PageInfoUtil;
import com.wintersun.util.UploadUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wintersun.controller.BaseController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BaseController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    UserCollectionService userCollectionService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    UploadUtil uploadUtil;

    @Autowired
    WsService wsService;

    @Autowired
    SearchService searchService;


    @Autowired
    ChatService chatService;

    @Autowired
    AmqpTemplate amqpTemplate;


    public Page getPage(){
        int pn = ServletRequestUtils.getIntParameter(request, "pn", PageInfoUtil.PAGE_NUM);
        int size = ServletRequestUtils.getIntParameter(request, "size", PageInfoUtil.PAGE_SIZE);

        Page page = new Page(pn,size);

        return page;
    }

    protected AccountProfile getProfile(){

        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    protected Long getProfileId(){
        return getProfile().getId();
    }

}
