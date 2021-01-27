package com.tuling.springmvc.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String value() default "";
}
