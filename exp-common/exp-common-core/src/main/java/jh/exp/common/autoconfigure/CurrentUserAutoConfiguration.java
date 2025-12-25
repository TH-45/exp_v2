package jh.exp.common.autoconfigure;

import jh.exp.common.filter.CurrentUserFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 自动注册 CurrentUserFilter：
 * - 仅在 Servlet Web 应用中生效（Spring MVC）。
 * - 通过读取网关透传的请求头，将用户信息写入 {@code CurrentUserHolder(ThreadLocal)}。
 *
 * 注意：对于 WebFlux（Reactive）应用不适用 ThreadLocal，需要用 Reactor Context（此处不注册）。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({FilterRegistrationBean.class, jakarta.servlet.Filter.class})
public class CurrentUserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CurrentUserFilter.class)
    public CurrentUserFilter currentUserFilter() {
        return new CurrentUserFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "currentUserFilterRegistration")
    public FilterRegistrationBean<CurrentUserFilter> currentUserFilterRegistration(CurrentUserFilter filter) {
        FilterRegistrationBean<CurrentUserFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        // 尽量靠前执行，确保业务代码里能拿到 CurrentUserHolder
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("currentUserFilter");
        return registration;
    }
}


