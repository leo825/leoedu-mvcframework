package com.leoedu.demo.service.impl;

import com.leoedu.demo.service.IDemoService;
import com.leoedu.mvcframework.annotation.MyService;

/**
 * Created by Administrator on 2019/2/18.
 */
@MyService
public class DemoService implements IDemoService{
    @Override
    public String get(String name) {
        return "TOM";
    }
}
