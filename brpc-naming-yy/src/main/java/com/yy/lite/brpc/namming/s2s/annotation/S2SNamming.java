package com.yy.lite.brpc.namming.s2s.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})//作用于类上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface S2SNamming {
    String name() default "";
    int soReadTimeout() default 1000;
}
