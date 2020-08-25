package com.wintersun.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wintersun.common.lang.Result;
import com.wintersun.entity.User;
import com.wintersun.mapper.UserMapper;
import com.wintersun.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wintersun.shiro.AccountProfile;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Result register(User user) {

//        判断数据库是否已经存在

        int count = this.count(new QueryWrapper<User>()
                .eq("username", user.getUsername())
                .or()
                .eq("email", user.getEmail()));


        if(count > 0){
            return Result.fail("用户名或邮箱已存在");
        }

//        设置user对象，避免客户端传递新的无关数据，确保安全
        User temp = new User();
        temp.setUsername(user.getUsername());
        temp.setEmail(user.getEmail());
        temp.setPassword(SecureUtil.md5(user.getPassword()));
        temp.setAvatar("/res/images/avatar/default.png");

        temp.setCreated(new Date());
        temp.setPoint(0);
        temp.setVipLevel(0);
        temp.setCommentCount(0);
        temp.setPostCount(0);
        temp.setGender("0");

//        添加数据库
        this.save(temp);


        return Result.success();
    }

    @Override
    public AccountProfile login(String email, String password) {

        User user = this.getOne(new QueryWrapper<User>().eq("email", email));

        if(user == null){
            throw new UnknownAccountException();
        }

        if(!user.getPassword().equals(password)){
            throw new IncorrectCredentialsException();
        }

        user.setLasted(new Date());

        this.updateById(user);

        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user,profile);

        return profile;
    }
}
