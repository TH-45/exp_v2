package jh.exp.auth.clinet;

import cn.hutool.json.JSONUtil;
import jh.exp.auth.clinet.api.bus.AccountService;
import jh.exp.auth.clinet.api.bus.OrgUnitService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.clinet.api.bus.RoleService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration("AuthApi")
@AutoConfiguration
public class ApiConfiguration {
    @Bean("authLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "authLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder authLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    HttpServiceProxyFactory authHttpServiceProxyFactory(
            @Qualifier("authLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.auth.url:http://exp-auth}") String authUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(authUrl)
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
    PersonService personService(HttpServiceProxyFactory authHttpServiceProxyFactory) {
        return authHttpServiceProxyFactory.createClient(PersonService.class);
    }

    @Bean
    RoleService roleService(HttpServiceProxyFactory authHttpServiceProxyFactory) {
        return authHttpServiceProxyFactory.createClient(RoleService.class);
    }

    @Bean
    AccountService accountService(HttpServiceProxyFactory authHttpServiceProxyFactory) {
        return authHttpServiceProxyFactory.createClient(AccountService.class);
    }

    @Bean
    OrgUnitService orgUnitService(HttpServiceProxyFactory authHttpServiceProxyFactory) {
        return authHttpServiceProxyFactory.createClient(OrgUnitService.class);
    }

}
