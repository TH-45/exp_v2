package jh.exp.auth.clinet;

import cn.hutool.json.JSONUtil;
import jh.exp.auth.clinet.api.RoleService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import jh.exp.auth.clinet.api.PersonService;
@Configuration("AuthApi")
@AutoConfiguration
public class ApiConfiguration {
    private final HttpServiceProxyFactory httpServiceProxyFactory;

    public ApiConfiguration(RestClient.Builder restClientBuilder,
                            @Value("${exp.service.auth.url}") String authUrl){
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(authUrl)
                .defaultHeader(ServiceContext.REQUEST_SOURCE_HEADER, JSONUtil.toJsonStr(CurrentUserHolder.get()))
                .build();
        this.httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }

    @Bean
    PersonService personService() {
        return httpServiceProxyFactory.createClient(PersonService.class);
    }

    @Bean
    RoleService roleService() {
        return httpServiceProxyFactory.createClient(RoleService.class);
    }


}
