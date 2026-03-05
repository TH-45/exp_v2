package jh.exp.sys.client;

import cn.hutool.json.JSONUtil;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import jh.exp.sys.client.api.storage.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration("SysApi")
@AutoConfiguration
public class ApiConfiguration {
    @Bean("sysLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "sysLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder sysLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    HttpServiceProxyFactory sysHttpServiceProxyFactory(
            @Qualifier("sysLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.sys.url:http://exp-sys}") String sysUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(sysUrl)
                .requestInterceptor((request, body, execution) -> {
                    Object currentUser = CurrentUserHolder.get();
                    if (currentUser != null) {
                        request.getHeaders().set(ServiceContext.REQUEST_SOURCE_HEADER, JSONUtil.toJsonStr(currentUser));
                    }
                    return execution.execute(request, body);
                })
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }

    @Bean
    StorageService storageService(HttpServiceProxyFactory sysHttpServiceProxyFactory) {
        return sysHttpServiceProxyFactory.createClient(StorageService.class);
    }
}
