package jh.exp.bid.contract.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sys 服务安全配置：关闭默认登录重定向，统一放行由网关鉴权。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(registry -> registry
                        // 允许健康检查
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 其余接口均由网关鉴权后访问
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
