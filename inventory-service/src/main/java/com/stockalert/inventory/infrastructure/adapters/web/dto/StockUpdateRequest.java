package com.stockalert.inventory.infrastructure.adapters.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockUpdateRequest {

    @NotNull(message = "amount is required")
    @Min(value = 1, message = "amount must be >= 1")
    private Integer amount;
}
