package com.yy.lite.brpc.spring.configure;

import com.yy.lite.brpc.interceptor.BrpcConsumerAomiInterceptor;
import com.yy.lite.brpc.interceptor.BrpcMetricsInterceptor;
import com.yy.lite.brpc.interceptor.BrpcProviderAomiInterceptor;
import com.yy.lite.framework.aop.MetricsAop;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrpcMetricsInterceptorConfiguration {


    @Bean(name="brpcMetricsInterceptor")
    public BrpcMetricsInterceptor brpcMetricsInterceptor(){
        BrpcMetricsInterceptor aop = new BrpcMetricsInterceptor();
        return aop;
    }
    @Bean(name="brpcConsumerAomiInterceptor")
    public BrpcConsumerAomiInterceptor brpcConsumerAomiInterceptor(){
        BrpcConsumerAomiInterceptor aop = new BrpcConsumerAomiInterceptor();
        return aop;
    }
    @Bean(name="brpcProviderAomiInterceptor")
    public BrpcProviderAomiInterceptor brpcProviderAomiInterceptor(){
        BrpcProviderAomiInterceptor aop = new BrpcProviderAomiInterceptor();
        return aop;
    }


}