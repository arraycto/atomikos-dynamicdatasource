package com.frank.xa.annotation;

import java.lang.annotation.*;

/**
 * 动态数据源注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    /**
     * 数据源key
     * @return
     */
    String value() default "master";
}
