package com.stockalert.product.domain.ports.out;

import com.stockalert.product.domain.model.ProductStockChangedEvent;

/**
 * Output port for publishing product events.
 * This interface defines the contract for event publishing without any
 * implementation details (could be RabbitMQ, Kafka, SNS, etc.).
 * 
 * Following hexagonal architecture, the domain layer defines this contract
 * and the infrastructure layer will provide the implementation.
 */
public interface ProductEventPublisherPort {
    
    /**
     * Publishes a stock change event to the messaging system.
     * This method should be non-blocking and handle failures gracefully.
     * 
     * @param event the stock change event to publish
     * @throws IllegalArgumentException if the event is null or invalid
     */
    void publishStockChangeEvent(ProductStockChangedEvent event);
    
    /**
     * Publishes a generic product event.
     * Can be used for other product-related events in the future.
     * 
     * @param event the product event to publish
     */
    void publishProductEvent(ProductStockChangedEvent event);
}
