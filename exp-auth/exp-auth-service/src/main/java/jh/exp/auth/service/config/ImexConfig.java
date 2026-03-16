package jh.exp.auth.service.config;

import jh.exp.common.core.imex.ImexTaskManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImexConfig {

    @Bean
    public ImexTaskManager imexTaskManager() {
        return new ImexTaskManager();
    }
}
