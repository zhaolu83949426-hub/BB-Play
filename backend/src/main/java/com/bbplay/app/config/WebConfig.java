package com.bbplay.app.config;

import com.bbplay.app.interceptor.UserIdentityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地联调用跨域配置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserIdentityInterceptor userIdentityInterceptor;

    public WebConfig(UserIdentityInterceptor userIdentityInterceptor) {
        this.userIdentityInterceptor = userIdentityInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截 /api/user/** 路径，自动识别用户身份
        registry.addInterceptor(userIdentityInterceptor)
            .addPathPatterns("/api/user/**");
    }
}
