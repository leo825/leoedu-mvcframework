package com.leoedu.demo.mvc.proxy;

import com.leoedu.demo.mvc.service.DemoService;
import com.leoedu.demo.mvc.service.UserService;
import com.leoedu.demo.mvc.service.impl.DemoServiceImpl;
import com.leoedu.demo.mvc.service.impl.UserServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * JDK动态代理实现了InvocatHandler
 */
public class JdkProxy implements InvocationHandler {

    /**
     * 需要代理的目标对象
     */
    private Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("JDK动态代理,监听开始");
        Object result = method.invoke(target, args);
        System.out.println("JDK动态代理,监听结束!");
        return result;
    }

    //定义获取代理对象方法
    private Object getJDKProxy(Object targetObject) {
        //为目标对象target赋值
        this.target = targetObject;
        //JDK动态代理只能针对实现了接口的类进行代理，newProxyInstance函数所需参数就可以看出
        return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(), targetObject.getClass().getInterfaces(), this);
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String mehtodGet = "get";
        UserService userService = new UserServiceImpl();
        Method method;

        //实例化JDKProxy对象
        JdkProxy jdkProxy = new JdkProxy();
        UserService user = (UserService) jdkProxy.getJDKProxy(userService);
        method = userService.getClass().getMethod(mehtodGet,String.class);
        method.invoke(userService,"22");

        System.out.println("---------------分割线------------------");
        DemoService demoService = new DemoServiceImpl();
        DemoService demo = (DemoService) jdkProxy.getJDKProxy(demoService);
        method = demoService.getClass().getMethod(mehtodGet,String.class);
        method.invoke(demoService,"22");
    }
}
