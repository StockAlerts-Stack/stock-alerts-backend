package com.stockalert.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer threshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void decrease(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive");
        }
        if (this.quantity - amount < 0) {
            throw new IllegalStateException("Insufficient stock for productId=" + productId);
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be positive");
        }
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isStockLow() {
        return this.quantity <= this.threshold;
    }
}
