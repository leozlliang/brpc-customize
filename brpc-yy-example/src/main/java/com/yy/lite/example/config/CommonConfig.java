package com.yy.lite.example.config;

import com.yy.common.interceptor.CustomInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author donghonghua
 * @date 2019/8/1
 */
@Configuration
public class CommonConfig {

    @Bean
    @Qualifier("logInterceptor")
    public CustomInterceptor logInterceptor() {
        return new CustomInterceptor();
    }
}
