package com.stockalert.inventory.infrastructure.adapters.web.mapper;

import com.stockalert.inventory.domain.model.Inventory;
import com.stockalert.inventory.infrastructure.adapters.web.dto.InventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .threshold(inventory.getThreshold())
                .stockLow(inventory.isStockLow())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
