package com.stockalert.inventory.domain.ports.in;

import com.stockalert.inventory.domain.model.Inventory;

public interface CreateInventoryUseCase {

    Inventory create(Long productId, Integer quantity, Integer threshold);
}
