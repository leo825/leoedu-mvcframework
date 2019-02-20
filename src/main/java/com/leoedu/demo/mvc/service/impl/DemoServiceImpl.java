package com.leoedu.demo.mvc.service.impl;

import com.leoedu.demo.mvc.service.DemoService;
import com.leoedu.mvcframework.annotation.MyService;

/**
 * Created by Administrator on 2019/2/18.
 */
@MyService
public class DemoServiceImpl implements DemoService {

    @Override
    public String get(String id) {
        System.out.println("获取id==" + id);
        return "获取id==" + id;
    }
}
