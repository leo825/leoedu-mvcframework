package com.leoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2019/2/18.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}
