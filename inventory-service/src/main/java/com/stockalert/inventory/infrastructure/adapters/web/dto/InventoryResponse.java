package com.stockalert.inventory.infrastructure.adapters.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer threshold;
    private boolean stockLow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
