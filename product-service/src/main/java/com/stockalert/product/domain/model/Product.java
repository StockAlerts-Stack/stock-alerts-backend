package com.stockalert.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain entity representing a Product.
 * This is a pure Java object without any framework dependencies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
    
    /**
     * Validates if the product has all required fields
     */
    public boolean isValid() {
        return sku != null && !sku.trim().isEmpty()
                && name != null && !name.trim().isEmpty()
                && price != null && price.compareTo(BigDecimal.ZERO) >= 0
                && stock != null && stock >= 0;
    }
    
    /**
     * Updates the stock quantity
     */
    public void updateStock(Integer newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = newStock;
    }
    
    /**
     * Updates the price
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
    }
}
