package com.tuling.springmvc.logic.service.impl;

import com.tuling.springmvc.ioc.annotation.Service;
import com.tuling.springmvc.logic.bean.User;
import com.tuling.springmvc.logic.service.UserService;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Override
    public String findUser() {
        System.out.println("==========调用findUser成功");
        return null;
    }

    @Override
    public User getUser() {
        return new User(1,"aaa","admin");
    }
}
