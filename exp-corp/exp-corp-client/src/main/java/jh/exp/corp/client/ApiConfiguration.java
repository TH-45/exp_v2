package jh.exp.corp.client;

import cn.hutool.json.JSONUtil;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.client.api.CompanyContactClientService;
import jh.exp.corp.client.api.QualificationAttachmentClientService;
import jh.exp.corp.client.api.QualificationClientService;
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

@Configuration("CorpApi")
@AutoConfiguration
public class ApiConfiguration {
    @Bean("corpLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "corpLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder corpLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    HttpServiceProxyFactory corpHttpServiceProxyFactory(
            @Qualifier("corpLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.corp.url:http://exp-corp}") String corpUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(corpUrl)
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
    CompanyClientService companyClientService(HttpServiceProxyFactory corpHttpServiceProxyFactory) {
        return corpHttpServiceProxyFactory.createClient(CompanyClientService.class);
    }

    @Bean
    CompanyContactClientService companyContactClientService(HttpServiceProxyFactory corpHttpServiceProxyFactory) {
        return corpHttpServiceProxyFactory.createClient(CompanyContactClientService.class);
    }

    @Bean
    QualificationClientService qualificationClientService(HttpServiceProxyFactory corpHttpServiceProxyFactory) {
        return corpHttpServiceProxyFactory.createClient(QualificationClientService.class);
    }

    @Bean
    QualificationAttachmentClientService qualificationAttachmentClientService(HttpServiceProxyFactory corpHttpServiceProxyFactory) {
        return corpHttpServiceProxyFactory.createClient(QualificationAttachmentClientService.class);
    }
}
