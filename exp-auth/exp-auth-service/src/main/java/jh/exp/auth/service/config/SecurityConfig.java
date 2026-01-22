package jh.exp.auth.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 简化版安全配置：当前阶段 auth 服务只提供内部接口，统一放行。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 注意：auth 服务引入了 Spring Security 依赖后，如果不显式声明 SecurityFilterChain，
     * 将启用默认安全策略，导致内部接口（如 /internal/auth/login）直接返回 401。
     *
     * 当前阶段：仅对网关等内部调用提供能力，因此放行 /internal/auth/**。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/internal/auth/**").permitAll()
                        // 允许健康检查
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 其余接口按需再收紧（当前保持放行，避免开发联调被默认策略拦截）
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


