package com.stockalert.inventory.infrastructure.adapters.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be >= 0")
    private Integer quantity;

    @NotNull(message = "threshold is required")
    @Min(value = 0, message = "threshold must be >= 0")
    private Integer threshold;
}
