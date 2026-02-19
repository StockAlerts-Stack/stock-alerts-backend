package com.stockalert.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event representing a change in product stock.
 * This is a pure domain object used for event-driven communication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockChangedEvent {
    
    /**
     * Product SKU (Stock Keeping Unit)
     */
    private String sku;
    
    /**
     * Product name
     */
    private String name;
    
    /**
     * Previous stock quantity
     */
    private Integer oldStock;
    
    /**
     * New stock quantity
     */
    private Integer newStock;
    
    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Event type to distinguish between creation and update
     */
    private EventType eventType;
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Type of stock change event
     */
    public enum EventType {
        PRODUCT_CREATED,
        STOCK_UPDATED,
        STOCK_INCREASED,
        STOCK_DECREASED
    }
    
    /**
     * Calculate the stock difference
     */
    public Integer getStockDifference() {
        if (oldStock == null || newStock == null) {
            return null;
        }
        return newStock - oldStock;
    }
    
    /**
     * Check if stock was increased
     */
    public boolean isStockIncreased() {
        return oldStock != null && newStock != null && newStock > oldStock;
    }
    
    /**
     * Check if stock was decreased
     */
    public boolean isStockDecreased() {
        return oldStock != null && newStock != null && newStock < oldStock;
    }
}
