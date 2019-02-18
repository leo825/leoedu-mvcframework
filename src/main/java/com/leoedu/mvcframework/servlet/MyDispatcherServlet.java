package com.leoedu.mvcframework.servlet;

import com.leoedu.mvcframework.annotation.MyAutowired;
import com.leoedu.mvcframework.annotation.MyController;
import com.leoedu.mvcframework.annotation.MyRequestMapping;
import com.leoedu.mvcframework.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by Administrator on 2019/2/18.
 */
public class MyDispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    //跟web.xml中param-name的值一致
    private static final String LOCATION = "contextConfigLocation";
    //保存所有配置信息
    private Properties p = new Properties();
    //保存所有扫描到的相关类名
    private List<String> classNames = new ArrayList<String>();
    //核心IOC容器,保存素有初始化Bean
    private Map<String, Object> ioc = new HashMap<String, Object>();
    //保存所有的Url和相关方法的映射
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    /**
     * 初始化加载配置文件
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        long startTime = System.currentTimeMillis();

        /**
         * 1.加载配置文件
         */
        doLoadConfig(config.getInitParameter(LOCATION));
        /**
         * 2.扫描所有相关的类
         */
        doScanner(p.getProperty("scanPackage"));
        /**
         * 3.初始化所有相关类的实例，并保存到IOC容器中
         */
        doInstance();
        /**
         * 4。依赖注入
         */
        doAutowired();
        /**
         * 5.构造HandlerMapping
         */
        initHandleMapping();
        /**
         * 6.等待请求，匹配URL，定位方法，反射调用执行
         * 调用doGet或者doPost方法
         */
        System.out.println("leoedu-mvcframework初始化完成,耗时 " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * 将文件读到Properties对象中
     *
     * @param location
     */
    private void doLoadConfig(String location) {
        InputStream fis = null;
        try {
            fis = this.getClass().getClassLoader().getResourceAsStream(location);
            //读取配置文件
            p.load(fis);
            //p.setProperty("scanPackage", "com.leoedu.demo");
            System.out.println(p.getProperty("scanPackage"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归扫描出所有的Class文件
     *
     * @param packageName
     */
    private void doScanner(String packageName) {
        //将所有的包路径转换成文件路径
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            //如果是文件夹，继续递归
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    /**
     * doInstance()方法，初始化所有相关的类，并放入到IOC容器之中。
     * IOC容器的key默认是类名首字母小写，如果是自己设置类名，
     * 则优先使用自定义的。因此，要先写一个针对类名首字母处理的工具方法
     */
    private void doInstance() {
        if (classNames.size() == 0) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    //默认将首字符小写作为beanName
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    //如果用户设置了名字，就用用户自己设置
                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, clazz.newInstance());
                        continue;
                    }
                    //如果自己没设置，就按照接口类型创建一个实例
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将初始化到IOC容器中的类，需赋值的字段进行赋值
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //拿到实例对象中的所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                //设置私有属性的访问权限
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * 将MyRequestMapping中配置的信息和Method进行关联，并保存这些关系。
     */
    private void initHandleMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //获取Method的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }

                //映射URL
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("[mapped]===" + url + "," + method);
            }
        }
    }


    /**
     * 将首字符转小写
     *
     * @param str
     * @return
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    /**
     * 执行业务处理
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            doDispath(request, response);
        } catch (Exception e) {
            //如果匹配过程出现异常，将异常打印出去
            response.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll("\\s", "\r\n"));
        }

    }

    /**
     * 具体处理
     *
     * @param request
     * @param response
     * @throws Exception
     */
    protected void doDispath(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.handlerMapping.isEmpty()) {
            return;
        }
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        System.out.println("url==="+url);
        if (!this.handlerMapping.containsKey(url)) {
            response.getWriter().write("404 Not Found");
            return;
        }

        Method method = this.handlerMapping.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];
        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            //根据参数名称,做某些处理
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                //参数类型已明确，这边强转类型
                paramValues[i] = request;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = response;
                continue;
            } else if (parameterType == String.class) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        try {
            String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
            //利用反射机制来调用
            method.invoke(this.ioc.get(beanName), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
