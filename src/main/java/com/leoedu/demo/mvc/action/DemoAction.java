package com.leoedu.demo.mvc.action;

import com.leoedu.demo.service.IDemoService;
import com.leoedu.mvcframework.annotation.MyAutowired;
import com.leoedu.mvcframework.annotation.MyController;
import com.leoedu.mvcframework.annotation.MyRequestMapping;
import com.leoedu.mvcframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2019/2/18.
 */
@MyController
@MyRequestMapping("/demo")
public class DemoAction {

    @MyAutowired
    private IDemoService demoService;

    @MyRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/get.json")
    public void get(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
