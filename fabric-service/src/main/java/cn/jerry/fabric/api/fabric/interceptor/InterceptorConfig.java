package cn.jerry.fabric.api.fabric.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminIdentityInterceptor())
                .addPathPatterns("/user/**", "/mgt/**");
        registry.addInterceptor(new UserIdentityInterceptor())
                .addPathPatterns("/cert/**", "/tx/**");
    }

}
