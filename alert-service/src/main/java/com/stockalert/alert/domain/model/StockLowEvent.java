package com.stockalert.alert.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Incoming event payload published by the Inventory Service
 * when product stock falls at or below the configured threshold.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLowEvent {

    private Long productId;
    private int currentStock;
    private int threshold;
    private LocalDateTime timestamp;
}
