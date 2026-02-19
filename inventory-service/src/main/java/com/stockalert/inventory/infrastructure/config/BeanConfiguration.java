package com.stockalert.inventory.infrastructure.config;

import com.stockalert.inventory.domain.ports.out.InventoryRepositoryPort;
import com.stockalert.inventory.domain.ports.out.StockEventPublisherPort;
import com.stockalert.inventory.domain.service.InventoryDomainService;
import com.stockalert.inventory.infrastructure.messaging.RabbitMQEventPublisher;
import com.stockalert.inventory.infrastructure.persistence.InventoryJpaRepository;
import com.stockalert.inventory.infrastructure.persistence.InventoryRepositoryAdapter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public InventoryRepositoryPort inventoryRepositoryPort(InventoryJpaRepository jpaRepository) {
        return new InventoryRepositoryAdapter(jpaRepository);
    }

    @Bean
    public StockEventPublisherPort stockEventPublisherPort(RabbitTemplate rabbitTemplate) {
        return new RabbitMQEventPublisher(rabbitTemplate);
    }

    @Bean
    public InventoryDomainService inventoryDomainService(
            InventoryRepositoryPort inventoryRepositoryPort,
            StockEventPublisherPort stockEventPublisherPort) {
        return new InventoryDomainService(inventoryRepositoryPort, stockEventPublisherPort);
    }
}
