package com.leoedu.demo.mvc.service.impl;

import com.leoedu.demo.mvc.service.DemoService;
import com.leoedu.demo.mvc.service.UserService;


/**
 * 用户管理实现类，实现用户管理接口
 */
public class UserServiceImpl implements UserService {
    @Override
    public void addUser(String userName, String password) {
        System.out.println("调用了新增的方法！");
        System.out.println("传入参数为 userName: "+userName+" password: "+password);
    }

    @Override
    public void delUser(String userName) {
        System.out.println("调用了删除的方法！");
        System.out.println("传入参数为 userName: "+userName);
    }

    @Override
    public String get(String name) {
        System.out.println("实现了Demo方法" + name);
        return "实现了Demo方法" + name;
    }
}
