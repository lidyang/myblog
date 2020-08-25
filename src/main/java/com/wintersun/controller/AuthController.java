package com.wintersun.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.code.kaptcha.Producer;
import com.wintersun.common.lang.Result;
import com.wintersun.entity.User;
import com.wintersun.util.ValidationUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
public class AuthController extends  BaseController{

    public static final String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";

    @Autowired
    private Producer producer;

    @GetMapping("kaptcha.jpg")
    public void kaptcha(HttpServletResponse response) throws IOException {
        String text = producer.createText();
        request.getSession().setAttribute(KAPTCHA_SESSION_KEY,text);
        BufferedImage image = producer.createImage(text);
        response.setHeader("Cache-Control","no-store,no-cache");
        response.setContentType("image/jepg");

        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(image,"jpg",outputStream);



    }

    @GetMapping("/login")
    public String login(){
        return "/auth/login";
    }


    @ResponseBody
    @PostMapping("/login")
    public Result doLogin(String email, String password){

        if(StrUtil.isEmpty(email) || StrUtil.isBlank(password)) {
            return Result.fail("邮箱或密码不能为空");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(email, SecureUtil.md5(password));
        try {
            SecurityUtils.getSubject().login(token);

        } catch (AuthenticationException e) {
            if (e instanceof UnknownAccountException) {
                return Result.fail("用户不存在");
            } else if (e instanceof LockedAccountException) {
                return Result.fail("用户被禁用");
            } else if (e instanceof IncorrectCredentialsException) {
                return Result.fail("密码错误");
            } else {
                return Result.fail("用户认证失败");
            }
        }


        return Result.success().action("/");
    }





    @GetMapping("/register")
    public String register(){

        return "/auth/reg";
    }


    @ResponseBody
    @PostMapping("/register")
    public Result doRegister(User user, String repass, String vercode){

        System.out.println(user);
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(user);

        if(validResult.hasErrors()){
            return Result.fail(validResult.getErrors());
        }

        if( !user.getPassword().equals(repass) ){
            return Result.fail("两次输入密码不一致！");
        }

        String kaptchaCode = (String) request.getSession().getAttribute(KAPTCHA_SESSION_KEY);
        System.out.println("kaptchaCode：" + kaptchaCode);
        if(vercode  != null && !vercode.equalsIgnoreCase(kaptchaCode)){
            return Result.fail("输入验证码不正确！");
        }

        Result result = userService.register(user);
        request.getSession().setAttribute(KAPTCHA_SESSION_KEY,null);

        return result.action("/login");
    }

    @RequestMapping("/user/logout")
    public String logout(){

        SecurityUtils.getSubject().logout();
        return "redirect:/";
    }





}
