package com.combine.auditorium.config;

import com.combine.auditorium.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login", 
                        "/api/auth/register", 
                        "/api/live/callback/**", // 排除 SRS 回调接口
                        "/api/ai/**", // 临时排除 AI 接口以方便测试，或者在测试时记得带 Token
                        "/error", 
                        "/doc.html", 
                        "/webjars/**", 
                        "/v3/api-docs/**", 
                        "/swagger-resources/**"
                );
    }
}

