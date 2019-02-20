package com.leoedu.demo.mvc.service;


/**
 * 用户管理接口
 */
public interface UserService {
    //新增用户抽象方法
    void addUser(String userName,String password);
    //删除用户抽象方法
    void delUser(String userName);
    //获取id的对象
    String get(String id);
}
