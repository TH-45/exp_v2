package jh.exp.project.client;

import cn.hutool.json.JSONUtil;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import jh.exp.project.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration("ProjectApi")
@AutoConfiguration
public class ApiConfiguration {
    private final HttpServiceProxyFactory httpServiceProxyFactory;

    public ApiConfiguration(RestClient.Builder restClientBuilder,
                            @Value("${exp.service.project.url}") String projectUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(projectUrl)
                .defaultHeader(ServiceContext.REQUEST_SOURCE_HEADER, JSONUtil.toJsonStr(CurrentUserHolder.get()))
                .build();
        this.httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }

    @Bean
    ProjectClientService projectClientService() {
        return httpServiceProxyFactory.createClient(ProjectClientService.class);
    }

    @Bean
    PersonProjectRelClientService personProjectRelClientService() {
        return httpServiceProxyFactory.createClient(PersonProjectRelClientService.class);
    }

    @Bean
    ProjectRoleCfgClientService projectRoleCfgClientService() {
        return httpServiceProxyFactory.createClient(ProjectRoleCfgClientService.class);
    }

    @Bean
    ProjectStaffAssignClientService projectStaffAssignClientService() {
        return httpServiceProxyFactory.createClient(ProjectStaffAssignClientService.class);
    }

    @Bean
    ProjectMaterialPlanClientService projectMaterialPlanClientService() {
        return httpServiceProxyFactory.createClient(ProjectMaterialPlanClientService.class);
    }

    @Bean
    ProjectMaterialStockClientService projectMaterialStockClientService() {
        return httpServiceProxyFactory.createClient(ProjectMaterialStockClientService.class);
    }

    @Bean
    ProjectMaterialUsageClientService projectMaterialUsageClientService() {
        return httpServiceProxyFactory.createClient(ProjectMaterialUsageClientService.class);
    }

    @Bean
    ProjectScheduleClientService projectScheduleClientService() {
        return httpServiceProxyFactory.createClient(ProjectScheduleClientService.class);
    }

    @Bean
    ProjectScheduleLogClientService projectScheduleLogClientService() {
        return httpServiceProxyFactory.createClient(ProjectScheduleLogClientService.class);
    }
}
