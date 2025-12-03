package jh.exp.gateway.config;

import jh.exp.gateway.auth.client.AuthInternalHttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 基于 WebClient 的下游服务 HTTP 客户端配置。
 *
 * - 使用 @LoadBalanced WebClient 通过服务名（如 http://exp-auth）访问下游服务；
 * - 通过 HttpServiceProxyFactory 为 @HttpExchange 接口生成代理，实现声明式调用。
 */
@Configuration
public class HttpClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public AuthInternalHttpClient authInternalHttpClient(WebClient.Builder builder) {
        // 创建 WebClient
        WebClient client = builder
                // 通过服务名访问 exp-auth 服务，结合 Nacos + Spring Cloud LoadBalancer 进行发现与负载均衡
                .baseUrl("http://exp-auth")
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builder()
                .clientAdapter(WebClientAdapter.create(client))
                .build();
        return factory.createClient(AuthInternalHttpClient.class);
    }
}


