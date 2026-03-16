package jh.exp.bid.contract.client;

import jh.exp.bid.contract.client.api.ContractClient;
import jh.exp.bid.contract.client.api.AwardResultClient;
import jh.exp.common.core.constant.CommonConstant;
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
 * 合同服务客户端自动配置，参考 exp-process-client、exp-corp-client 方式
 */
@Configuration("BidContractApi")
@AutoConfiguration
public class ApiConfiguration {

    @Bean("bidContractLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "bidContractLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder bidContractLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("bidContractHttpServiceProxyFactory")
    HttpServiceProxyFactory bidContractHttpServiceProxyFactory(
            @Qualifier("bidContractLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.bid-contract.url:http://exp-bid-contract}") String bidContractUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(bidContractUrl)
                .requestInterceptor((request, body, execution) -> {
                    // 透传当前请求的 Authorization 至合同服务
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
    ContractClient contractClient(
            @Qualifier("bidContractHttpServiceProxyFactory") HttpServiceProxyFactory bidContractHttpServiceProxyFactory) {
        return bidContractHttpServiceProxyFactory.createClient(ContractClient.class);
    }

    @Bean
    AwardResultClient awardResultClient(
            @Qualifier("bidContractHttpServiceProxyFactory") HttpServiceProxyFactory bidContractHttpServiceProxyFactory) {
        return bidContractHttpServiceProxyFactory.createClient(AwardResultClient.class);
    }
}
