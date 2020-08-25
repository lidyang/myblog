package com.wintersun.service;

import com.wintersun.common.lang.Result;
import com.wintersun.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wintersun.shiro.AccountProfile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
public interface UserService extends IService<User> {

    Result register(User user);

    AccountProfile login(String email, String password);

}
