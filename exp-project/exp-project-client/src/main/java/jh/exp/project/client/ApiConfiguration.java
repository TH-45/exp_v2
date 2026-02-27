package jh.exp.project.client;

import cn.hutool.json.JSONUtil;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.constant.ServiceContext;
import jh.exp.project.client.api.*;
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

@Configuration("ProjectApi")
@AutoConfiguration
public class ApiConfiguration {
    @Bean("projectLoadBalancedRestClientBuilder")
    @ConditionalOnMissingBean(name = "projectLoadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder projectLoadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    HttpServiceProxyFactory projectHttpServiceProxyFactory(
            @Qualifier("projectLoadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${exp.service.project.url:http://exp-project}") String projectUrl) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(projectUrl)
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
    ProjectClientService projectClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectClientService.class);
    }

    @Bean
    PersonProjectRelClientService personProjectRelClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(PersonProjectRelClientService.class);
    }

    @Bean
    ProjectRoleCfgClientService projectRoleCfgClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectRoleCfgClientService.class);
    }

    @Bean
    ProjectStaffAssignClientService projectStaffAssignClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectStaffAssignClientService.class);
    }

    @Bean
    ProjectMaterialPlanClientService projectMaterialPlanClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectMaterialPlanClientService.class);
    }

    @Bean
    ProjectMaterialStockClientService projectMaterialStockClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectMaterialStockClientService.class);
    }

    @Bean
    ProjectMaterialUsageClientService projectMaterialUsageClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectMaterialUsageClientService.class);
    }

    @Bean
    ProjectScheduleClientService projectScheduleClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectScheduleClientService.class);
    }

    @Bean
    ProjectScheduleLogClientService projectScheduleLogClientService(HttpServiceProxyFactory projectHttpServiceProxyFactory) {
        return projectHttpServiceProxyFactory.createClient(ProjectScheduleLogClientService.class);
    }
}
