package jh.exp.bid.contract.service.config;

import jakarta.servlet.http.HttpServletRequest;
import jh.exp.common.core.constant.CommonConstant;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;

/**
 * RestTemplate 配置，用于调用流程等服务（透传当前请求的 Authorization）
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate processRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setInterceptors(Collections.singletonList(new AuthForwardInterceptor()));
        return rt;
    }

    /** 透传当前请求的 Authorization 头至下游服务 */
    private static class AuthForwardInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest sr = attrs.getRequest();
                if (sr != null) {
                    String auth = sr.getHeader(CommonConstant.AUTH_HEADER_NAME);
                    if (auth != null && !auth.isEmpty()) {
                        request.getHeaders().set(CommonConstant.AUTH_HEADER_NAME, auth);
                    }
                }
            }
            return execution.execute(request, body);
        }
    }
}
