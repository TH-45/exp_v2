package jh.exp.corp.client;

import cn.hutool.json.JSONUtil;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.client.api.CompanyContactClientService;
import jh.exp.corp.client.api.QualificationAttachmentClientService;
import jh.exp.corp.client.api.QualificationClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration("CorpApi")
@AutoConfiguration
public class ApiConfiguration {
    private final HttpServiceProxyFactory httpServiceProxyFactory;

    public ApiConfiguration(RestClient.Builder restClientBuilder,
                            @Value("${exp.service.corp.url}") String corpUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(corpUrl)
                .defaultHeader(ServiceContext.REQUEST_SOURCE_HEADER, JSONUtil.toJsonStr(CurrentUserHolder.get()))
                .build();
        this.httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }

    @Bean
    CompanyClientService companyClientService() {
        return httpServiceProxyFactory.createClient(CompanyClientService.class);
    }

    @Bean
    CompanyContactClientService companyContactClientService() {
        return httpServiceProxyFactory.createClient(CompanyContactClientService.class);
    }

    @Bean
    QualificationClientService qualificationClientService() {
        return httpServiceProxyFactory.createClient(QualificationClientService.class);
    }

    @Bean
    QualificationAttachmentClientService qualificationAttachmentClientService() {
        return httpServiceProxyFactory.createClient(QualificationAttachmentClientService.class);
    }
}
