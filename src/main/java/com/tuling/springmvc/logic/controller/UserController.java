package com.tuling.springmvc.logic.controller;

import com.tuling.springmvc.ioc.annotation.Autowired;
import com.tuling.springmvc.ioc.annotation.Controller;
import com.tuling.springmvc.ioc.annotation.RequestMapping;
import com.tuling.springmvc.ioc.annotation.ResponseBody;
import com.tuling.springmvc.logic.bean.User;
import com.tuling.springmvc.logic.service.UserService;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 */
@Controller
public class UserController {

    @Autowired(value = "userService")
    private UserService userService;

    // 定义方法
    @RequestMapping("/findUser")
    public String findUser() {
        userService.findUser();
        return null;
    }

    @RequestMapping("/getData")
    @ResponseBody  //返回json格式的数据
    public User getData(){
        //调用服务层
        return userService.getUser();
    }
}
