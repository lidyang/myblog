package com.wintersun.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.common.lang.Result;
import com.wintersun.entity.Post;
import com.wintersun.entity.User;
import com.wintersun.entity.UserMessage;
import com.wintersun.shiro.AccountProfile;
import com.wintersun.util.PageInfoUtil;
import com.wintersun.util.UploadUtil;
import com.wintersun.vo.CommentVo;
import com.wintersun.vo.PostVo;
import com.wintersun.vo.UserMessageVo;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class UserController extends  BaseController{

    @RequestMapping("/user/home")
    public String home(){
        User user = userService.getById(getProfileId());
        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .eq("user_id", getProfileId())
                .gt("created", DateUtil.offsetDay(new Date(), 30))
                .orderByDesc("created"));

        request.setAttribute("user", user);
        request.setAttribute("posts", posts);

        return "/user/home";
    }


    @RequestMapping("/user/set")
    public String index(){
        User user = userService.getById(getProfileId());
        request.setAttribute("user", user);
        return "/user/set";
    }


    @ResponseBody
    @PostMapping("/user/set")
    public Result doSet(User user){

        if(StrUtil.isNotBlank(user.getAvatar())) {
            User temp = userService.getById(getProfileId());
            temp.setAvatar(user.getAvatar());
            userService.updateById(temp);

            AccountProfile profile = getProfile();
            profile.setAvatar(user.getAvatar());

//            SecurityUtils.getSubject().getSession().setAttribute("profile", profile);

            return Result.success().action("/user/set#avatar");
        }
        if(StrUtil.isBlank(user.getUsername())) {
            return Result.fail("昵称不能为空");
        }

        int count = userService.count(new QueryWrapper<User>()
                .eq("username", getProfile().getUsername())
                .ne("id", getProfileId()));

        if(count > 0){
            return Result.fail("改昵称已经被使用！");
        }

//        更新数据库
        User temp = userService.getById(getProfileId());
        temp.setUsername(user.getUsername());
        temp.setSign(user.getSign());
        temp.setGender(user.getGender());
        userService.updateById(temp);

        //重新保存到session中
        BeanUtil.copyProperties(temp,getProfile());

        return Result.success().action("/user/set#info");
    }

    @ResponseBody
    @PostMapping("/user/upload")
    public Result uploadAvatar(@RequestParam(value = "file") MultipartFile file) throws IOException {
        return uploadUtil.upload(UploadUtil.type_avatar, file);
    }


    @ResponseBody
    @PostMapping("/user/repass")
    public Result repass(String nowpass, String pass, String repass){
        if(!pass.equals(repass)) {
            return Result.fail("两次密码不相同");
        }

        User user = userService.getById(getProfileId());

        String nowPassMd5 = SecureUtil.md5(nowpass);
        if(!nowPassMd5.equals(user.getPassword())) {
            return Result.fail("输入原密码不正确");
        }

        user.setPassword(SecureUtil.md5(pass));
        userService.updateById(user);

        return Result.success().action("/user/repass#info");
    }



    @RequestMapping("/user/index")
    public String set(){
        return "/user/index";
    }

    @ResponseBody
    @GetMapping("/user/public")
    public Result userPublic(){

        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .inSql("id","select post_id from user_collection where user_id = " + getProfileId())
                .orderByDesc("created"));
        return Result.success(page);
    }


    @ResponseBody
    @GetMapping("/user/collection")
    public Result userCollection(){

        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .eq("user_id", getProfileId())
                .orderByDesc("created"));
        return Result.success(page);
    }



    @GetMapping("/user/mess")
    public String mess() {

        IPage<UserMessageVo> page = userMessageService.paging(getPage(), new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileId())
                .orderByDesc("created")
        );
        // 把消息改成已读状态
        List<Long> ids = new ArrayList<>();
        for(UserMessageVo messageVo : page.getRecords()) {
            if(messageVo.getStatus() == 0) {
                ids.add(messageVo.getId());
            }
        }
        // 批量修改成已读
        userMessageService.updateToReaded(ids);

        request.setAttribute("pageData", page);
        return "/user/mess";
    }


    @PostMapping("/message/remove")
    public Result remove(Long id,@RequestParam(defaultValue = "false") Boolean all) {

        boolean remove = userMessageService.remove(new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileId())
                .eq(!all, "id", id));
        return remove ? Result.success() : Result.fail("删除失败");
    }

    @ResponseBody
    @PostMapping("/message/nums")
    public Map messageNums(){

        int count = userMessageService.count(new QueryWrapper<UserMessage>()
                .eq("to_user_id",getProfileId())
                .eq("status","0"));
        return MapUtil.builder("status",0)
                    .put("count",count).build();
    }




}
