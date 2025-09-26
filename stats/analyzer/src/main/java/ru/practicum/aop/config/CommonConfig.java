package ru.practicum.aop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class CommonConfig {
    @Bean
    @RequestScope
    public RequestScopeObject requestScopeBean() {
        return new RequestScopeObject();
    }
}


