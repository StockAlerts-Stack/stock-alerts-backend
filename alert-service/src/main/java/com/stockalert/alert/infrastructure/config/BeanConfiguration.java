package com.stockalert.alert.infrastructure.config;

import com.stockalert.alert.domain.ports.out.AlertRepositoryPort;
import com.stockalert.alert.domain.service.AlertDomainService;
import com.stockalert.alert.infrastructure.persistence.AlertJpaRepository;
import com.stockalert.alert.infrastructure.persistence.AlertRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public AlertRepositoryPort alertRepositoryPort(AlertJpaRepository jpaRepository) {
        return new AlertRepositoryAdapter(jpaRepository);
    }

    @Bean
    public AlertDomainService alertDomainService(AlertRepositoryPort alertRepositoryPort) {
        return new AlertDomainService(alertRepositoryPort);
    }
}
