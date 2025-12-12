package jh.exp.common.Config;

import jh.exp.common.audit.interceptor.AuthContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthContextInterceptor authContextInterceptor;

    public WebMvcConfig(AuthContextInterceptor authContextInterceptor) {
        this.authContextInterceptor = authContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对所有 API 路径启用拦截器，排除您可能有的特定白名单
        registry.addInterceptor(authContextInterceptor).addPathPatterns("/**");
    }
}