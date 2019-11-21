package com.yy.lite.brpc.namming.s2s.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BrpcApiContent {
    //serviceTag
    String serviceTag() default "";
    /***
     * 接口超时时间
     * @return
     */
    int readTimeoutMillis() default 0;

    /***
     *
     * 接口传输超时时间
     * @return
     */
    int writeTimeoutMillis() default 0;
}
