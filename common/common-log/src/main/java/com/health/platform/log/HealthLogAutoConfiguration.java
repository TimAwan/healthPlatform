package com.health.platform.log;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HealthLogAutoConfiguration {

    @Bean
    public OperationLogAspect operationLogAspect(ObjectProvider<OperationLogClient> clientProvider) {
        return new OperationLogAspect(clientProvider);
    }
}
