package com.stockalert.product.infrastructure.adapters.out.messaging;

import com.stockalert.product.domain.model.ProductStockChangedEvent;
import com.stockalert.product.domain.ports.out.ProductEventPublisherPort;
import com.stockalert.product.infrastructure.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ implementation of ProductEventPublisherPort.
 * 
 * Publishes product stock change events to RabbitMQ exchange for consumption
 * by other microservices (alert-service, notification-service, etc.).
 * 
 * This is the primary implementation used in production environments.
 */
@Slf4j
@Primary
@Component
public class RabbitMQStockPublisherAdapter implements ProductEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQStockPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishStockChangeEvent(ProductStockChangedEvent event) {
        publishProductEvent(event);
    }

    @Override
    public void publishProductEvent(ProductStockChangedEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event, skipping");
            return;
        }

        try {
            log.info("Publishing product event to RabbitMQ: {} - Product: {} (SKU: {}), Stock: {} → {}", 
                event.getEventType(), 
                event.getName(), 
                event.getSku(),
                event.getOldStock(), 
                event.getNewStock());

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EVENTS_EXCHANGE,
                RabbitMQConfig.PRODUCT_STOCK_ROUTING_KEY,
                event
            );

            log.debug("Successfully published event: {}", event);

        } catch (AmqpException e) {
            log.error("Failed to publish product event to RabbitMQ: {} - Error: {}", 
                event, e.getMessage(), e);
            
            // In production, you might want to:
            // 1. Store failed events in a dead-letter queue
            // 2. Retry with exponential backoff
            // 3. Send alerts to monitoring system
            // For now, we log the error and continue (fail-safe approach)
        } catch (Exception e) {
            log.error("Unexpected error while publishing event: {}", event, e);
        }
    }
}
