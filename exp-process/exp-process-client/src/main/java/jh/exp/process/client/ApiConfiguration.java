package jh.exp.process.client;

import jh.exp.common.core.constant.CommonConstant;
import jh.exp.process.client.api.ProcessApprovalClient;
import jh.exp.process.client.api.ProcessDefinitionClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程服务客户端自动配置，参考 auth-client、corp-client 方式
 */
@Configuration("ProcessApi")
@AutoConfiguration
public class ApiConfiguration {

    @Bean("processLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "processLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder processLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("processHttpServiceProxyFactory")
    HttpServiceProxyFactory processHttpServiceProxyFactory(
            @Qualifier("processLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.process.url:http://exp-process}") String processUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(processUrl)
                .requestInterceptor((request, body, execution) -> {
                    // 透传当前请求的 Authorization 至流程服务
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
                })
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }

    @Bean
    ProcessApprovalClient processApprovalClient(
            @Qualifier("processHttpServiceProxyFactory") HttpServiceProxyFactory processHttpServiceProxyFactory) {
        return processHttpServiceProxyFactory.createClient(ProcessApprovalClient.class);
    }

    @Bean
    ProcessDefinitionClient processDefinitionClient(
            @Qualifier("processHttpServiceProxyFactory") HttpServiceProxyFactory processHttpServiceProxyFactory) {
        return processHttpServiceProxyFactory.createClient(ProcessDefinitionClient.class);
    }
}
