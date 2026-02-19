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
public class StockLowEvent {

    private Long productId;
    private Integer currentStock;
    private Integer threshold;
    private LocalDateTime timestamp;
}
