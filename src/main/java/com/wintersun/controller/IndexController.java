package com.wintersun.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.util.PageInfoUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController extends BaseController{

    @RequestMapping({"","/","index"})
    public String index(){
        System.out.println("method: index " );

//      1.分页信息、2.分类、3.用户、4.置顶、5.精选、6.排序
        IPage results = postService.paging(getPage(),null,null,null,null,"created");


        request.setAttribute("pageData",results);
        request.setAttribute("currentCategoryId",0);
        return "index";
    }

    @RequestMapping("/search")
    public String search(String q) {

        IPage pageData = searchService.search(getPage(), q);

        request.setAttribute("q", q);
        request.setAttribute("pageData", pageData);
        return "search";
    }


}
