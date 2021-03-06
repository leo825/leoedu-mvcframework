package com.leoedu.demo.mvc.action;

import com.leoedu.demo.mvc.service.DemoService;
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
    private DemoService demoService;

    @MyRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        try {
            String result = demoService.get(name);
            req.setCharacterEncoding("utf-8");
            resp.setContentType("text/html;charset=utf-8");
            resp.setCharacterEncoding("utf-8");
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/get.json")
    public void get(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        try {
            String result = demoService.get(name);
            req.setCharacterEncoding("utf-8");
            resp.setContentType("text/html;charset=utf-8");
            resp.setCharacterEncoding("utf-8");
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
