package com.stockalert.product.infrastructure.adapters.out.messaging;

import com.stockalert.product.domain.model.ProductStockChangedEvent;
import com.stockalert.product.domain.ports.out.ProductEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of ProductEventPublisherPort for development and testing.
 * This adapter logs events instead of publishing them to a real message broker.
 * 
 * Activated when property 'messaging.stub.enabled=true' is set.
 * Otherwise, the RabbitMQ adapter (marked with @Primary) is used.
 * 
 * Useful for local development and testing without requiring RabbitMQ infrastructure.
 */
@Component
@ConditionalOnProperty(name = "messaging.stub.enabled", havingValue = "true", matchIfMissing = false)
public class ProductEventPublisherStub implements ProductEventPublisherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventPublisherStub.class);
    
    @Override
    public void publishStockChangeEvent(ProductStockChangedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        logger.info("📢 [EVENT PUBLISHED] Stock Change Event: {}", formatEvent(event));
    }
    
    @Override
    public void publishProductEvent(ProductStockChangedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        logger.info("📢 [EVENT PUBLISHED] Product Event: {}", formatEvent(event));
    }
    
    /**
     * Formats the event for logging
     */
    private String formatEvent(ProductStockChangedEvent event) {
        return String.format(
            "{ type: %s, sku: '%s', productId: %d, oldStock: %d, newStock: %d, difference: %d, timestamp: %s }",
            event.getEventType(),
            event.getSku(),
            event.getProductId(),
            event.getOldStock(),
            event.getNewStock(),
            event.getStockDifference(),
            event.getTimestamp()
        );
    }
}
